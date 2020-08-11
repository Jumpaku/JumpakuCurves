package jumpaku.curves.fsc.merge

import jumpaku.commons.control.None
import jumpaku.commons.control.Option
import jumpaku.commons.control.Some
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.Knot
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.generate.extendBack
import jumpaku.curves.fsc.generate.extendFront
import jumpaku.curves.fsc.generate.fit.WeightedParamPoint
import jumpaku.curves.fsc.merge.Merger.Companion.weightByKde
import java.util.*
import kotlin.math.abs
/*
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

    val detector: OverlapDetector = OverlapDetector(overlapThreshold)

    fun tryMerge(existing: BSpline, overlapping: BSpline): Option<BSpline> {
        val existSamples = existing.sample(samplingSpan)
        val overlapSamples = overlapping.sample(samplingSpan)
        val overlapState = detector.detect(existSamples, overlapSamples).orNull() ?: return None
        val data = MergeData.parameterize2(existSamples, overlapSamples, mergeRate, overlapState)
        return Some(generate(data))
    }

    fun generate(mergeData: MergeData): BSpline {
        val data = mergeData.aggregated
                .let { extendFront(it, extendInnerSpan, extendOuterSpan, extendDegree) }
                .let { extendBack(it, extendInnerSpan, extendOuterSpan, extendDegree) }
        val extendedDomain = Interval(data.first().param, data.last().param)
        val knotVector = KnotVector.clamped(extendedDomain, degree, knotSpan)
        return Generator.generate(data, knotVector, fuzzifier)
                .run { restrict(mergeData.domain) }
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
*/
