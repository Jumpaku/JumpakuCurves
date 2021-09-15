package jumpaku.curves.core.transform

import jumpaku.commons.control.Option
import jumpaku.curves.core.geom.Point
import kotlin.math.abs
import kotlin.math.sqrt

fun Translate.asSimilarity(): SimilarityTransform = SimilarityTransform(this)

fun Rotate.asSimilarity(): SimilarityTransform = SimilarityTransform(this)

fun UniformlyScale.asSimilarity(): SimilarityTransform = SimilarityTransform(this)

class SimilarityTransform internal constructor(private val affine: AffineTransform) : (Point) -> Point {

    constructor() : this(AffineTransform.Identity)

    override fun invoke(p: Point): Point = affine(p).copy(r = p.r * scale())

    fun invert(): Option<SimilarityTransform> = affine.invert().map(::SimilarityTransform)

    fun scale(): Double = scales(affine).average()

    fun andThen(similarity: SimilarityTransform): SimilarityTransform =
        SimilarityTransform(affine.andThen(similarity.affine))

    fun at(pivot: Point): SimilarityTransform = SimilarityTransform(affine.at(pivot))

    fun asAffine(): AffineTransform = affine

    companion object {

        val Identity = SimilarityTransform(AffineTransform.Identity)

        private fun scales(transform: AffineTransform): List<Double> {
            val m = transform.matrix.data
            val s0 = sqrt(m[0][0] * m[0][0] + m[1][0] * m[1][0] + m[2][0] * m[2][0])
            val s1 = sqrt(m[0][1] * m[0][1] + m[1][1] * m[1][1] + m[2][1] * m[2][1])
            val s2 = sqrt(m[0][2] * m[0][2] + m[1][2] * m[1][2] + m[2][2] * m[2][2])
            return listOf(s0, s1, s2)
        }

        fun isSimilarity(transform: AffineTransform, tolerance: Double = 1e-10): Boolean {
            val (s0, s1, s2) = scales(transform)
            if (abs(s0 - s1) > tolerance) return false
            if (abs(s1 - s2) > tolerance) return false
            if (abs(s2 - s0) > tolerance) return false
            return true
        }

        fun from(transform: AffineTransform, tolerance: Double): SimilarityTransform {
            require(isSimilarity(transform, tolerance))
            return SimilarityTransform(transform)
        }
    }
}

