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
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

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

    val detector: OverlapDetector2 = OverlapDetector2(overlapThreshold, mergeRate)

    fun tryMerge(fsc0: BSpline, fsc1: BSpline): Option<BSpline> {
        val samples0 = fsc0.sample(samplingSpan)
        val samples1 = fsc1.sample(samplingSpan)
        val state = when (val found = detector.detect(samples0, samples1)) {
            is OverlapState2.NotFound -> return None
            else -> found as OverlapState2.Found
        }
        val (overlapBegin0, overlapBegin1) = state.coreRidge.first()
        val (overlapEnd0, overlapEnd1) = state.coreRidge.last()
        val (transitionBegin0, transitionBegin1) = state.transitionBegin
        val (transitionEnd0, transitionEnd1) = state.transitionEnd
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

        val data = resample(state.coreRidge, fsc0, segmentation0, fsc1, segmentation1)
        val weighted = weightData(data)
        return Some(generate(weighted))
    }

    fun weightData(data: List<ParamPoint>): List<WeightedParamPoint> {
        fun kernel(u: Double): Double = if (abs(u) > 1) 0.0 else (1 - u * u) * (1 - u * u) * 15 / 16

        val params = data.map { it.param }
        val n = params.size
        val m = params.average()
        val s2 = params.sumByDouble { (it - m) * (it - m) } / (n - 1)
        val s = sqrt(s2)
        val h = s * 1.06 * n.toDouble().pow(0.2)
        println(h)

        fun density(i: Int): Double {
            var fi = 0.0
            for (j in i downTo 0) {
                val kj = kernel((params[i] - params[j]) / h)
                if (kj > 0) fi += kj else break
            }
            for (j in i until n) {
                val kj = kernel((params[i] - params[j]) / h)
                if (kj > 0) fi += kj else break
            }
            return fi / (n * h)
        }

        return data.mapIndexed { i, p -> p.weighted(1 / density(i)) }
    }

    fun resample(
            coreRidge: OverlapRidge,
            fsc0: BSpline,
            segmentation0: DomainSegmentation,
            fsc1: BSpline,
            segmentation1: DomainSegmentation
    ): List<ParamPoint> {
        val mergeData = resampleMergeData(coreRidge, fsc0, fsc1).sortedBy { it.param }
        val transition0 = resampleTransitionData(fsc0, segmentation0, mergeData)
        val transition1 = resampleTransitionData(fsc1, segmentation1, mergeData)
        val mergeAndTransitionData = (transition0 + transition1 + mergeData).sortedBy { it.param }
        val remain0 = resampleRemainData(fsc0, segmentation0, mergeAndTransitionData)
        val remain1 = resampleRemainData(fsc1, segmentation1, mergeAndTransitionData)
        return (remain0 + remain1 + mergeAndTransitionData).sortedBy { it.param }
    }

    fun resampleMergeData(coreRidge: OverlapRidge, fsc0: BSpline, fsc1: BSpline): List<ParamPoint> {
        val samples0 = fsc0.sample(samplingSpan)
        val samples1 = fsc1.sample(samplingSpan)
        val ridgeComponents = coreRidge.fold(mutableListOf<MutableList<Pair<Int, Int>>>()) { acc, (i, j) ->
            when {
                acc.isEmpty() -> acc.add(mutableListOf(i to j))
                acc.last().last().let { (ii, jj) -> ii != i && jj != j } -> acc.add(mutableListOf(i to j))
                else -> acc.last().add(i to j)
            }
            acc
        }
        val mergeData = ridgeComponents.flatMap { component ->
            val idx0 = component.map { (i, _) -> i }.distinct()
            val idx1 = component.map { (_, j) -> j }.distinct()
            val tau = samplingSpan / 2
            val begin0 = samples0[idx0.first()].param - tau
            val end0 = samples0[idx0.last()].param + tau
            val begin1 = samples1[idx1.first()].param - tau
            val end1 = samples1[idx1.last()].param + tau
            val n0 = idx0.size
            val n1 = idx1.size
            val data0 = idx0.mapIndexed { k, i ->
                val t = begin1.lerp((k + 0.5) / (n0 + 1), end1).coerceIn(fsc1.domain)
                samples0[i].lerp(mergeRate, ParamPoint(fsc1(t), t))
            }
            val data1 = idx1.mapIndexed { k, j ->
                val t = begin0.lerp((k + 0.5) / (n1 + 1), end0).coerceIn(fsc0.domain)
                samples1[j].lerp(1 - mergeRate, ParamPoint(fsc0(t), t))
            }
            data0 + data1
        }
        return mergeData//coreRidge.map { (i, j) -> samples0[i].lerp(mergeRate, samples1[j]) }
    }

    fun resampleTransitionData(fsc: BSpline,
                               segmentation: DomainSegmentation,
                               mergeData: List<ParamPoint>
    ): List<ParamPoint> {
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
                ParamPoint(p, param)
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
                ParamPoint(p, param)
            }
        }
        return (transitionFront + transitionBack)
    }

    fun resampleRemainData(
            fsc: BSpline,
            segmentation: DomainSegmentation,
            mergeAndTransitionData: List<ParamPoint>
    ): List<ParamPoint> {
        val remainFront = if (segmentation.remainFront.span <= 0) emptyList()
        else {
            val transitionBegin = mergeAndTransitionData.first()
            val samples = segmentation.remainFront.sample(samplingSpan)
            val delta = transitionBegin.param - segmentation.remainFront.end
            samples.map { t -> ParamPoint(fsc(t), t + delta) }
        }
        val remainBack = if (segmentation.remainBack.span <= 0) emptyList()
        else {
            val transitionEnd = mergeAndTransitionData.last()
            val samples = segmentation.remainBack.sample(samplingSpan)
            val delta = transitionEnd.param - segmentation.remainBack.begin
            samples.map { t -> ParamPoint(fsc(t), t + delta) }
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

