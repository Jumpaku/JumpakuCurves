package jumpaku.curves.fsc.identify.nquarter.reference

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.fsc.identify.primitive.reference.CircularGenerator
import jumpaku.curves.fsc.identify.primitive.reference.Reference
import org.apache.commons.math3.util.FastMath

class NQuarterCircular : NQuarterGenerator {

    override fun <C : Curve> generate(n: Int, fsc: ReparametrizedCurve<C>): Reference {
        val nQuarterWeight = nQuarterWeight(n)
        val s = fsc.originalCurve
        val (t0, t1) = s.domain
        val f = s(CircularGenerator.computeCircularFar(s, t0, t1))
        val b0 = s(t0)
        val b2 = s(t1)
        val m = b0.middle(b2)
        val l = b0.dist(b2) / 2
        val h = m.dist(f)
        val qh = l * FastMath.sqrt((1 - nQuarterWeight) / (1 + nQuarterWeight))
        val qf = m.lerp(qh / h, f).copy(r = f.r)
        return Reference(ConicSection(b0, qf, b2, nQuarterWeight))
    }
}