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

    val chordLength: Double
        get() = when (toArcLengthRatio) {
            is LinearFit -> originalCurve.invoke(toArcLengthRatio.originalParams).zipWithNext(Point::dist).sum()
            is QuadraticFit -> originalCurve.invoke(toArcLengthRatio.originalParams).zipWithNext(Point::dist).sum()
            else -> error("")
        }


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
            val ds = originalParams.map(originalCurve).zipWithNext(Point::dist)
            val ls = ds.scan(0.0) { acc, d -> acc + d }
            val rs = ls.map { (it / ls.last()).coerceIn(Interval.Unit) }
            val samples = if (rs.all { it.isFinite() }) originalParams.zip(rs) else originalParams.map { it to 0.0 }
            val toArcLengthRatio = LinearFit(samples)
            val toOriginal = LinearFit(samples.map { (t, s) -> s to t })
            return ReparametrizedCurve(originalCurve, toArcLengthRatio, toOriginal)
        }

        fun <C : Curve> of2(originalCurve: C, originalParams: List<Double>): ReparametrizedCurve<C> {
            val r = QuadraticFit.of(originalCurve, originalParams)

            return ReparametrizedCurve(originalCurve, r, QuadraticFit.Inverse(r))

        }
    }
}