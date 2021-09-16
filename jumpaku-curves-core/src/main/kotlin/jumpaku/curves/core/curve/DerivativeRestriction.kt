package jumpaku.curves.core.curve

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector

class DerivativeRestriction<D:Derivative>(val derivative: D, subDomain: Interval) : Derivative {

    override val domain: Interval = subDomain

    override fun invoke(t: Double): Vector {
        require(t in domain) { "t($t) must be in $domain" }
        return derivative.invoke(t)
    }

    override fun invoke(sortedParams: List<Double>): List<Vector> {
        require(sortedParams.all(domain::contains))
        return derivative.invoke(sortedParams)
    }
}