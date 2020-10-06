package jumpaku.curves.fsc.merge

import jumpaku.commons.control.None
import jumpaku.commons.control.Option
import jumpaku.commons.control.Some
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.lerp
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.generate.extendBack
import jumpaku.curves.fsc.generate.extendFront
import jumpaku.curves.fsc.generate.fit.WeightedParamPoint
import jumpaku.curves.fsc.generate.fit.weighted

data class DomainSegmentation(
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

        println(segmentation0)
        println(segmentation1)
        val data = resample(fsc0, segmentation0, fsc1, segmentation1)

        return Some(generate(data))
    }

    fun resample(
            fsc0: BSpline,
            segmentation0: DomainSegmentation,
            fsc1: BSpline,
            segmentation1: DomainSegmentation
    ): List<WeightedParamPoint> {
        val mergeData = resampleMergeData(fsc0, segmentation0, fsc1, segmentation1).sortedBy { it.param }
        val transition0 = resampleTransitionData(fsc0, segmentation0, mergeData)
        val transition1 = resampleTransitionData(fsc1, segmentation1, mergeData)
        val mergeAndTransitionData = (transition0 + transition1 + mergeData).sortedBy { it.param }
        val remain0 = resampleRemainData(fsc0, segmentation0, mergeAndTransitionData)
        val remain1 = resampleRemainData(fsc1, segmentation1, mergeAndTransitionData)
        return (remain0 + remain1 + mergeAndTransitionData).sortedBy { it.param }
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
        val transitionFront = if (segmentation.transitionFront.span <= 0) emptyList()
        else {
            val mergeBegin = mergeData.first()
            val samples = segmentation.transitionFront.sample(samplingSpan)
            val remainData = segmentation.transitionFront.sample(samplingSpan).map(fsc)
            val translate = mergeBegin.point - remainData.last()
            val transformed = remainData.map { it + translate }
            val transitionData = remainData.zip(transformed).mapIndexed { i, (p, q) ->
                val ratio = i / (samples.size - 1.0)
                p.lerp(ratio, q)
            }
            val remainDataLength = remainData.zipWithNext(Point::dist).sum()
            val transitionDataLength = transitionData.zipWithNext(Point::dist).sum()
            val transitionSpan = segmentation.transitionFront.span * transitionDataLength / remainDataLength
            val (transitionBegin, transitionEnd) = Interval(mergeBegin.param - transitionSpan, mergeBegin.param)
            transitionData.mapIndexed { i, p ->
                val ratio = i / (samples.size - 1.0)
                val param = (transitionBegin).lerp(ratio, transitionEnd)
                ParamPoint(p, param).weighted(transitionDataLength / remainDataLength)
            }
        }

        val transitionBack = if (segmentation.transitionBack.span <= 0) emptyList()
        else {
            val mergeEnd = mergeData.last()
            val samples = segmentation.transitionBack.sample(samplingSpan)
            val remainData = segmentation.transitionBack.sample(samplingSpan).map(fsc)
            val translate = mergeEnd.point - remainData.first()
            val transformed = remainData.map { it + translate }
            val transitionData = remainData.zip(transformed).mapIndexed { i, (p, q) ->
                val ratio = 1 - i / (samples.size - 1.0)
                p.lerp(ratio, q)
            }
            val remainDataLength = remainData.zipWithNext(Point::dist).sum()
            val transitionDataLength = transitionData.zipWithNext(Point::dist).sum()
            val transitionSpan = segmentation.transitionBack.span * transitionDataLength / remainDataLength
            val (transitionBegin, transitionEnd) = Interval(mergeEnd.param, mergeEnd.param + transitionSpan)
            transitionData.mapIndexed { i, p ->
                val ratio = 1 - i / (samples.size - 1.0)
                val param = (transitionBegin).lerp(1 - ratio, transitionEnd)
                ParamPoint(p, param).weighted(transitionDataLength / remainDataLength)
            }
        }
        return (transitionFront + transitionBack)
    }

    fun resampleRemainData(
            fsc: BSpline,
            segmentation: DomainSegmentation,
            mergeAndTransitionData: List<WeightedParamPoint>
    ): List<WeightedParamPoint> {
        val remainFront = if (segmentation.remainFront.span <= 0) emptyList()
        else {
            val transitionBegin = mergeAndTransitionData.first()
            val samples = segmentation.remainFront.sample(samplingSpan)
            val delta = transitionBegin.param - segmentation.remainFront.end
            samples.map { t -> ParamPoint(fsc(t), t + delta).weighted() }
        }
        val remainBack = if (segmentation.remainBack.span <= 0) emptyList()
        else {
            val transitionEnd = mergeAndTransitionData.last()
            val samples = segmentation.remainBack.sample(samplingSpan)
            val delta = transitionEnd.param - segmentation.remainBack.begin
            samples.map { t -> ParamPoint(fsc(t), t + delta).weighted() }
        }
        return remainFront + remainBack
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

