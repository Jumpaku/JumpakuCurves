package jumpaku.fsc.identify

import jumpaku.core.curve.Curve
import jumpaku.core.curve.arclength.ReparametrizedCurve
import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.identify.reference.Reference
import org.apache.commons.math3.util.FastMath


fun reparametrize(fsc: BSpline): ReparametrizedCurve<BSpline> = fsc.run {
    ReparametrizedCurve(fsc, knotVector.knots.map { it.value }.filter { it in domain })
}

interface Identifier {

    val nFmps: Int

    fun <C: Curve> identify(fsc: ReparametrizedCurve<C>): IdentifyResult
}