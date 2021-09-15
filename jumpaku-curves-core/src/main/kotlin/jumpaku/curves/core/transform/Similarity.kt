package jumpaku.curves.core.transform

import jumpaku.curves.core.geom.Point
import org.apache.commons.math3.linear.RealMatrix
import kotlin.math.abs
import kotlin.math.sqrt

fun Translate.similarity(): Similarity = Similarity(matrix)

fun Rotate.similarity(): Similarity = Similarity(matrix)

fun UniformlyScale.similarity(): Similarity = Similarity(matrix)

class Similarity internal constructor(override val matrix: RealMatrix) : Transform {

    constructor() : this(Transform.Identity.matrix)

    fun andThenSimilarly(translate: Translate): Similarity = Similarity(super.andThen(translate).matrix)

    fun andThenSimilarly(rotate: Rotate): Similarity = Similarity(super.andThen(rotate).matrix)

    fun andThenSimilarly(uniformlyScale: UniformlyScale): Similarity = Similarity(super.andThen(uniformlyScale).matrix)

    fun andThenSimilarly(similarity: Similarity): Similarity = Similarity(super.andThen(similarity).matrix)

    override fun at(origin: Point): Similarity = Similarity(super.at(origin).matrix)

    companion object {

        fun isSimilarity(transform: Transform, tolerance: Double = 1e-10): Boolean {
            val m = transform.matrix.data
            val s0 = sqrt(m[0][0] * m[0][0] + m[1][0] * m[1][0] + m[2][0] * m[2][0])
            val s1 = sqrt(m[0][1] * m[0][1] + m[1][1] * m[1][1] + m[2][1] * m[2][1])
            val s2 = sqrt(m[0][2] * m[0][2] + m[1][2] * m[1][2] + m[2][2] * m[2][2])
            if (abs(s0 - s1) > tolerance) return false
            if (abs(s1 - s2) > tolerance) return false
            if (abs(s2 - s0) > tolerance) return false
            return true
        }

        fun of(transform: Transform, tolerance: Double): Similarity {
            require(isSimilarity(transform, tolerance))
            return Similarity(transform.matrix)
        }
    }
}

