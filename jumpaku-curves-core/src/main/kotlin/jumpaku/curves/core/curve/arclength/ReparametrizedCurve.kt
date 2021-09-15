package jumpaku.curves.core.curve.arclength

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.Sampler
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point


/**
 * maps arc-length ratio parameter to point on original curve.
 */
class ReparametrizedCurve<C : Curve>(
    val originalCurve: C,
    val toArcLengthRatio: ParamConverter,
    val toOriginal: ParamConverter
) : Curve {

    override val domain: Interval = Interval.Unit

    override fun invoke(t: Double): Point {
        require(t in domain) { "t($t) is out of domain($domain)" }
        return originalCurve(toOriginal(t))
    }

    override fun invoke(sortedParams: List<Double>): List<Point> = originalCurve(toOriginal(sortedParams))

    fun <O : Curve> isPossible(other: ReparametrizedCurve<O>, n: Int): Grade =
        invoke(Sampler(n))
            .zip(other.invoke(Sampler(n)), Point::isPossible)
            .reduce(Grade::and)

    fun <O : Curve> isNecessary(other: ReparametrizedCurve<O>, n: Int): Grade =
        invoke(Sampler(n))
            .zip(other.invoke(Sampler(n)), Point::isNecessary)
            .reduce(Grade::and)

    companion object {

        fun <C : Curve> of(originalCurve: C, originalParams: List<Double>): ReparametrizedCurve<C> {
            val rs = MutableList(originalParams.size) { 0.0 }
            var prev = originalCurve(originalParams[0])
            for (i in 1..rs.lastIndex) {
                val cur = originalCurve(originalParams[i])
                rs[i] = rs[i - 1] + prev.dist(cur)
                prev = cur
            }
            val l = rs.last()
            for (i in rs.indices) {
                rs[i] = (rs[i] / l).coerceIn(0.0..1.0)
            }
            val toArcLengthRatio = LinearFit(originalParams, rs)
            val toOriginal = LinearFit(rs, originalParams)
            return ReparametrizedCurve(originalCurve, toArcLengthRatio, toOriginal)
        }

        fun <C : Curve> of2(originalCurve: C, originalParams: List<Double>): ReparametrizedCurve<C> {
            val r = QuadraticFit.of(originalCurve, originalParams)

            return ReparametrizedCurve(originalCurve, r, QuadraticFit.Inverse(r))

        }
    }
}