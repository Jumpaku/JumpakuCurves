package jumpaku.curves.fsc.identify.nquarter.reference

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.fsc.identify.primitive.reference.Reference
import org.apache.commons.math3.util.FastMath

interface NQuarterGenerator {

    fun <C : Curve> generate(n: Int, fsc: ReparametrizedCurve<C>): Reference
}

fun nQuarterWeight(n: Int): Double {
    require(n in 1..3) { "n($n) must be in [1, 2, 3]" }
    return FastMath.cos(0.25 * n * FastMath.PI)
}
