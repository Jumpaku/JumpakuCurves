package jumpaku.curves.fsc.merge

import jumpaku.commons.control.None
import jumpaku.commons.control.Option
import jumpaku.commons.control.Some
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.transformParams
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.lerp
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.generate.extendBack
import jumpaku.curves.fsc.generate.extendFront
import jumpaku.curves.fsc.generate.fit.BezierFitter
import jumpaku.curves.fsc.generate.fit.WeightedParamPoint
import jumpaku.curves.fsc.generate.fit.weighted
import kotlin.math.min

class DomainSegmentation(
        val remainFront: Interval,
        val transitionFront: Interval,
        val overlap: Interval,
        val transitionBack: Interval,
        val remainBack: Interval
) {
    companion object {

        fun segment(
                fsc: BSpline,
                transitionBegin: Double,
                transitionEnd: Double,
                overlapBegin: Double,
                overlapEnd: Double
        ): DomainSegmentation = DomainSegmentation(
                Interval(fsc.domain.begin, transitionBegin),
                Interval(transitionBegin, overlapBegin),
                Interval(overlapBegin, overlapEnd),
                Interval(overlapEnd, transitionEnd),
                Interval(transitionEnd, fsc.domain.end))
    }
}

class Merger2(
        val degree: Int = 3,
        val knotSpan: Double = 0.1,
        val extendDegree: Int = 2,
        val extendInnerSpan: Double = knotSpan * 2,
        val extendOuterSpan: Double = knotSpan * 2,
        val samplingSpan: Double = 0.01,
        val overlapThreshold: Grade = Grade.FALSE,
        val mergeRate: Double = 0.5,
        val bandWidth: Double = 0.01,
        val fuzzifier: Fuzzifier = Fuzzifier.Linear(
                velocityCoefficient = 0.86,
                accelerationCoefficient = 0.77)
) {

    init {
        require(degree >= 0)
        require(knotSpan > 0.0)
        require(extendDegree >= 0)
        require(extendInnerSpan > 0.0)
        require(extendOuterSpan > 0.0)
        require(samplingSpan > 0.0)
        require(mergeRate in 0.0..1.0)
        require(bandWidth > 0.0)
    }

    val detector: OverlapDetector2 = OverlapDetector2()

    fun tryMerge(fsc0: BSpline, fsc1: BSpline): Option<BSpline> {
        val samples0 = fsc0.sample(samplingSpan)
        val samples1 = fsc1.sample(samplingSpan)
        val (osm, baseRidge) = detector.detectBaseRidge(samples0, samples1, mergeRate, overlapThreshold)
        if (baseRidge !is Some) return None
        val overlapRidge = detector.detectDerivedRidge(osm, baseRidge.value, mergeRate, overlapThreshold)
                .orNull() ?: return None
        val extendedRidge = detector.detectDerivedRidge(osm, baseRidge.value, mergeRate, Grade.FALSE)
                .orNull() ?: return None

        val (overlapBegin0, overlapBegin1) = overlapRidge.ridge.first()
        val (overlapEnd0, overlapEnd1) = overlapRidge.ridge.last()
        val (transitionBegin0, transitionBegin1) = extendedRidge.ridge.first()
        val (transitionEnd0, transitionEnd1) = extendedRidge.ridge.last()
        val segmentation0 = DomainSegmentation.segment(fsc0,
                samples0[transitionBegin0].param,
                samples0[transitionEnd0].param,
                samples0[overlapBegin0].param,
                samples0[overlapEnd0].param
        )
        val segmentation1 = DomainSegmentation.segment(fsc1,
                samples1[transitionBegin1].param,
                samples1[transitionEnd1].param,
                samples1[overlapBegin1].param,
                samples1[overlapEnd1].param
        )

        val data = resample(fsc0, segmentation0, fsc1, segmentation1)

        return Some(generate(data))
    }

    fun resample(
            fsc0: BSpline,
            segmentation0: DomainSegmentation,
            fsc1: BSpline,
            segmentation1: DomainSegmentation
    ): List<WeightedParamPoint> {
        val mergeData = resampleMergeData(fsc0, segmentation0, fsc1, segmentation1)
        val merge = Interval(mergeData.first().param, mergeData.last().param)
        val transition0 = resampleTransitionData(fsc0, segmentation0, mergeData)
        val transition1 = resampleTransitionData(fsc1, segmentation1, mergeData)
        return emptyList()
    }

    fun resampleMergeData(fsc0: BSpline,
                          segmentation0: DomainSegmentation,
                          fsc1: BSpline,
                          segmentation1: DomainSegmentation
    ): List<WeightedParamPoint> {

        val nSamples = Interval(
                segmentation0.overlap.begin.lerp(mergeRate, segmentation1.overlap.begin),
                segmentation0.overlap.end.lerp(mergeRate, segmentation1.overlap.end)
        ).sample(samplingSpan).size
        val overlapSamples0 = segmentation0.overlap.sample(nSamples)
        val overlapSamples1 = segmentation1.overlap.sample(nSamples)
        return overlapSamples0.zip(overlapSamples1) { t0, t1 ->
            val p0 = fsc0(t0)
            val p1 = fsc1(t1)
            ParamPoint(p0.lerp(mergeRate, p1), t0.lerp(mergeRate, t1)).weighted(1.0)
        }
    }

    fun resampleTransitionData(fsc: BSpline,
                               segmentation: DomainSegmentation,
                               mergeData: List<WeightedParamPoint>
    ): List<WeightedParamPoint> {
        val mergeSpan = Interval(mergeData.first().param, mergeData.last().param)

        val innerSpan = min(mergeSpan.span, extendInnerSpan)
        val transitionFront = if (segmentation.transitionFront.span <= 0) emptyList()
        else {
            val samples = segmentation.transitionFront.sample(samplingSpan).map(fsc)
            val transitionSpan = segmentation.transitionFront.span
            val fitDataSubDomain = Interval(mergeSpan.begin - transitionSpan, mergeSpan.begin + innerSpan)
            val bezier = if (fitDataSubDomain.span > 0) {
                val fitData = mergeData.filter { it.param in fitDataSubDomain }.map {
                    val param = ((it.param - fitDataSubDomain.begin) / fitDataSubDomain.span).coerceIn(0.0, 1.0)
                    it.paramPoint.copy(param = param)
                }
                BezierFitter(extendDegree).fit(fitData)
            } else {
                Bezier(listOf(mergeData.first(), mergeData.first(), mergeData.first()).map { it.point })
            }
            val bezierSubDomain = Interval(0.0, (transitionSpan / fitDataSubDomain.span).coerceIn(0.0, 1.0))
            val extendData = bezier.restrict(bezierSubDomain).evaluateAll(samples.size)
            samples.zip(extendData).mapIndexed { i, (p, q) ->
                val ratio = i / (samples.size - 1.0)
                ParamPoint(p.lerp(ratio, q), segmentation.transitionFront.run { begin.lerp(ratio, end) })
            }
        }

        val transitionBack = if (segmentation.transitionBack.span <= 0) emptyList()
        else {
            val samples = segmentation.transitionBack.sample(samplingSpan).map(fsc)
            val transitionSpan = segmentation.transitionBack.span
            val fitDataSubDomain = Interval(mergeSpan.end - innerSpan, mergeSpan.end + transitionSpan)
            val bezier = if (fitDataSubDomain.span > 0) {
                val fitData = mergeData.filter { it.param in fitDataSubDomain }.map {
                    val param = ((it.param - fitDataSubDomain.begin) / fitDataSubDomain.span).coerceIn(0.0, 1.0)
                    it.paramPoint.copy(param = param)
                }
                BezierFitter(extendDegree).fit(fitData)
            } else {
                Bezier(listOf(mergeData.last(), mergeData.last(), mergeData.last()).map { it.point })
            }
            val bezierSubDomain = Interval((1 - transitionSpan / fitDataSubDomain.span).coerceIn(0.0, 1.0), 1.0)
            val extendData = bezier.restrict(bezierSubDomain).evaluateAll(samples.size)
            samples.zip(extendData).mapIndexed { i, (p, q) ->
                val ratio = 1 - i / (samples.size - 1.0)
                ParamPoint(p.lerp(ratio, q), segmentation.transitionFront.run { begin.lerp(ratio, end) })
            }
        }
        return (transitionFront + transitionBack).map { it.weighted() }
    }

    fun resample(
            existing: BSpline,
            overlapping: BSpline,
            overlapState: OverlapState
    ): List<WeightedParamPoint> {
        require(mergeRate in 0.0..1.0)
        val rowLast = overlapState.osm.rowLastIndex
        val columnLast = overlapState.osm.columnLastIndex
        val eSamples = existing.sample(rowLast + 1)
        val oSamples = overlapping.sample(columnLast + 1)

        val ridge = overlapState.ridge
        val (eMergeIdxBegin, oMergeIdxBegin) = ridge.first()
        val (eMergeIdxEnd, oMergeIdxEnd) = ridge.last()
        val (t0, t1, t2, t3) = listOf(0, eMergeIdxBegin, eMergeIdxEnd, rowLast).map { eSamples[it].param }
        val (s0, s1, s2, s3) = listOf(0, oMergeIdxBegin, oMergeIdxEnd, columnLast).map { oSamples[it].param }

        val u1 = t1.lerp(mergeRate, s1)
        val u2 = t2.lerp(mergeRate, s2)

        val eData = eSamples.mapNotNull {
            when (val t = it.param) {
                in t1..t2 -> null
                in t0..t1 -> it.copy(param = u1 - (t1 - t))
                in t2..t3 -> it.copy(param = u2 + (t - t2))
                else -> error("")
            }?.weighted()
        }
        val oData = oSamples.mapNotNull {
            when (val s = it.param) {
                in s1..s2 -> null
                in s0..s1 -> it.copy(param = u1 - (s1 - s))
                in s2..s3 -> it.copy(param = u2 + (s - s2))
                else -> error("")
            }?.weighted()
        }

        val nSamples = Interval(u1, u2).sample(samplingSpan).size
        val mData = Interval(t1, t2).sample(nSamples).zip(Interval(s1, s2).sample(nSamples)) { t, s ->
            val pE = existing(t)
            val pO = overlapping(s)
            val p = pE.lerp(mergeRate * (1 / pO.r) / (1 / pE.r + 1 / pO.r), pO)
            listOf(
                    ParamPoint(p, t.lerp(mergeRate, s)).weighted()
            )
        }.flatten()
        return (eData + oData + mData).sortedBy { it.param }
    }

    fun generate(mergeData: List<WeightedParamPoint>): BSpline {
        val domain = Interval(mergeData.first().param, mergeData.last().param)
        val data = mergeData
                .let { extendFront(it, extendInnerSpan, extendOuterSpan, extendDegree) }
                .let { extendBack(it, extendInnerSpan, extendOuterSpan, extendDegree) }
        val extendedDomain = Interval(data.first().param, data.last().param)
        val knotVector = KnotVector.clamped(extendedDomain, degree, knotSpan)
        return Generator.generate(data, knotVector, fuzzifier)
                .run { restrict(domain) }
    }

    companion object {

        fun derive(
                generator: Generator,
                samplingSpan: Double = 0.01,
                overlapThreshold: Grade = Grade.FALSE,
                mergeRate: Double = 0.5,
                bandWidth: Double = 0.01
        ): Merger2 = Merger2(
                degree = generator.degree,
                knotSpan = generator.knotSpan,
                extendDegree = generator.extendDegree,
                extendInnerSpan = generator.extendInnerSpan,
                extendOuterSpan = generator.extendOuterSpan,
                samplingSpan = samplingSpan,
                overlapThreshold = overlapThreshold,
                mergeRate = mergeRate,
                bandWidth = bandWidth,
                fuzzifier = generator.fuzzifier)
    }
}

