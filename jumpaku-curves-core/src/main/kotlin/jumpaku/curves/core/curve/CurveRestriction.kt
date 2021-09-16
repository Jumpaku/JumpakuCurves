package jumpaku.curves.core.curve

import jumpaku.curves.core.geom.Point

class CurveRestriction<C : Curve>(val originalCurve: C, subDomain: Interval) : Curve {

    override val domain: Interval = subDomain

    override fun invoke(t: Double): Point {
        require(t in domain) { "t($t) must be in $domain" }
        return originalCurve.invoke(t)
    }

    override fun invoke(sortedParams: List<Double>): List<Point> {
        require(sortedParams.all(domain::contains))
        return originalCurve.invoke(sortedParams)
    }
}