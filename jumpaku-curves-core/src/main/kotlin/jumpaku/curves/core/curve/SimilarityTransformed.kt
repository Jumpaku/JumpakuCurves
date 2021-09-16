package jumpaku.curves.core.curve

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.transform.SimilarityTransform


class SimilarityTransformed<C : Curve>(val originalCurve: C, val transform: SimilarityTransform) : Curve {

    override val domain: Interval get() = originalCurve.domain

    override fun invoke(t: Double): Point = transform(originalCurve(t))
}