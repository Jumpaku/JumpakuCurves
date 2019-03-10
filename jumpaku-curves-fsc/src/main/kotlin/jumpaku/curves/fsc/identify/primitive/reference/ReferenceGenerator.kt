package jumpaku.curves.fsc.identify.primitive.reference

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.rationalbezier.ConicSection


interface ReferenceGenerator {

    fun <C : Curve> generate(
            fsc: ReparametrizedCurve<C>,
            t0: Double = fsc.originalCurve.domain.begin,
            t1: Double = fsc.originalCurve.domain.end): Reference

    companion object {

        fun ellipticDomain(
                s0: Double,
                s1: Double,
                base: ReparametrizedCurve<ConicSection>,
                complement: ReparametrizedCurve<ConicSection>): Interval {
            val lc = complement.chordLength
            val l = base.chordLength
            val L = l / (s1 - s0)
            val b = complement.reparametrizer.run { -toOriginal((L * s0 / lc).coerceIn(range)) }
            val e = complement.reparametrizer.run { 2 - toOriginal((1 - (1 - s1) * L / lc).coerceIn(range)) }
            return Interval(b.coerceIn(Interval(-1.0, 2.0)), e.coerceIn(Interval(-1.0, 2.0)))
        }
    }
}

