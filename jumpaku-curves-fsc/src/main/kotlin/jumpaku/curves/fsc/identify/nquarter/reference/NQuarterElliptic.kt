package jumpaku.curves.fsc.identify.nquarter.reference

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.fsc.identify.primitive.reference.EllipticGenerator
import jumpaku.curves.fsc.identify.primitive.reference.Reference

class NQuarterElliptic(val nSamples: Int) : NQuarterGenerator {

    override fun <C : Curve> generate(n: Int, fsc: ReparametrizedCurve<C>): Reference {
        val qw = nQuarterWeight(n)
        val s = fsc.originalCurve
        val (t0, t1) = s.domain
        val tf = EllipticGenerator.computeEllipticFar(s, t0, t1, nSamples)
        return Reference(ConicSection(s(t0), s(tf), s(t1), qw))
    }
}
