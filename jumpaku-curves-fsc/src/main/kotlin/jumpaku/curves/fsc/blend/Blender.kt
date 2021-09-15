package jumpaku.curves.fsc.blend

import jumpaku.commons.control.None
import jumpaku.commons.control.Option
import jumpaku.commons.control.Some
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bspline.KnotVector
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.lerp
import jumpaku.curves.core.geom.middle
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.generate.extendBack
import jumpaku.curves.fsc.generate.extendFront
import jumpaku.curves.fsc.generate.fit.WeightedParamPoint
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil


sealed class BlendResult {

    abstract val overlapState: OverlapState

    class NotBlended(override val overlapState: OverlapState.NotDetected) : BlendResult()

    class Blended(override val overlapState: OverlapState.Detected, val blended: BSpline) : BlendResult()
}

/**
 * Blends two FSCs into a single FSC if they are overlapping.
 * The concept of this process is proposed in the following paper:
 * SATO, Y, YASUFUKU, N, SAGA, S. Sequential fuzzy spline curve generator for drawing interface by sketch. The Transactions of the Institute of Electronics, Information and Communication Engineers 2003;86(2):242–251. URL: https://ci.nii.ac.jp/naid/110003170883/en/
 */
class Blender(
    val degree: Int = 3,
    val knotSpan: Double = 0.1,
    val extendDegree: Int = 2,
    val extendInnerSpan: Double = knotSpan * 2,
    val extendOuterSpan: Double = knotSpan * 2,
    val samplingSpan: Double = 0.01,
    val overlapThreshold: Grade = Grade.FALSE,
    val blendRate: Double = 0.5,
    val bandWidth: Double = 0.05,
    val fuzzifier: Fuzzifier = Fuzzifier.Linear(
        velocityCoefficient = 0.86,
        accelerationCoefficient = 0.77
    )
) {

    data class SmallInterval(val begin: Double, val end: Double) : ClosedRange<Double> by Interval(begin, end) {

        val representative: Double = begin.middle(end)

        val span: Double = end - begin

        fun lerp(t: Double, that: SmallInterval): SmallInterval =
            SmallInterval(begin.lerp(t, that.begin), end.lerp(t, that.end))
    }

    class SampledCurve(val curve: BSpline, val spans: List<SmallInterval>) {

        val representativeParams: List<Double> = spans.map { it.representative }

        val representativePoints: List<Point> = curve(representativeParams)

        companion object {

            fun sample(curve: BSpline, samplingSpan: Double): SampledCurve =
                SampledCurve(curve, curve.domain.sample(samplingSpan).zipWithNext(::SmallInterval))
        }
    }

    init {
        require(degree >= 0)
        require(knotSpan > 0.0)
        require(extendDegree >= 0)
        require(extendInnerSpan > 0.0)
        require(extendOuterSpan > 0.0)
        require(samplingSpan > 0.0)
        require(blendRate in 0.0..1.0)
        require(bandWidth > 0.0)
    }

    val detector: OverlapDetector = OverlapDetector(overlapThreshold, blendRate)
    val parametrizer: OverlapParametrizer = OverlapParametrizer(samplingSpan, blendRate)

    fun tryBlend(existing: BSpline, overlapping: BSpline): BlendResult {
        val existingSampled =
            SampledCurve(existing, existing.domain.sample(samplingSpan).zipWithNext(::SmallInterval))
        val overlappingSampled =
            SampledCurve(overlapping, overlapping.domain.sample(samplingSpan).zipWithNext(::SmallInterval))
        val overlapState = detector.detect(existingSampled, overlappingSampled)
        val data = when (overlapState) {
            is OverlapState.NotDetected -> return BlendResult.NotBlended(overlapState)
            is OverlapState.Detected -> parametrizer.parametrize(existingSampled, overlappingSampled, overlapState)
        }
        return BlendResult.Blended(overlapState, generate(data))
    }


    fun generate(blendData: List<WeightedParamPoint>): BSpline {
        val domain = blendData.run { Interval(first().param, last().param) }
        val data = listOf(
            extendFront(blendData, extendInnerSpan, extendOuterSpan, extendDegree, samplingSpan),
            blendData,
            extendBack(blendData, extendInnerSpan, extendOuterSpan, extendDegree, samplingSpan)
        ).flatten().let { weightByKde(it, bandWidth) }

        val extendedDomain = Interval(data.first().param, data.last().param)
        val knotVector = KnotVector.clamped(extendedDomain, degree, knotSpan)
        return Generator.generate(data, knotVector, fuzzifier).restrict(domain)
    }

    companion object {

        fun weightByKde(paramPoints: List<WeightedParamPoint>, bandWidth: Double): List<WeightedParamPoint> {
            val n = paramPoints.size
            fun kernel(t: Double): Double = if (abs(t) > 1) 0.0 else 15 * (1 - t * t) * (1 - t * t) / 16
            val weights = mutableListOf<Double>()
            for ((i, p) in paramPoints.withIndex()) {
                var density = kernel(0.0)
                var j = i - 1
                while (j >= 0 && abs(paramPoints[j].param - p.param) / bandWidth < 1) {
                    density += kernel((paramPoints[j].param - p.param) / bandWidth)
                    --j
                }
                var k = i + 1
                while (k < paramPoints.size && abs(paramPoints[k].param - p.param) / bandWidth < 1) {
                    density += kernel((paramPoints[k].param - p.param) / bandWidth)
                    ++k
                }
                weights += n * bandWidth / density
            }
            return paramPoints.zip(weights) { wp, w -> wp.copy(weight = wp.weight * w) }
        }

        fun derive(
            generator: Generator,
            samplingSpan: Double = 0.01,
            overlapThreshold: Grade = Grade.FALSE,
            blendRate: Double = 0.5,
            bandWidth: Double = 0.05
        ): Blender = Blender(
            degree = generator.degree,
            knotSpan = generator.knotSpan,
            extendDegree = generator.extendDegree,
            extendInnerSpan = generator.extendInnerSpan,
            extendOuterSpan = generator.extendOuterSpan,
            samplingSpan = samplingSpan,
            overlapThreshold = overlapThreshold,
            blendRate = blendRate,
            bandWidth = bandWidth,
            fuzzifier = generator.fuzzifier
        )
    }
}

