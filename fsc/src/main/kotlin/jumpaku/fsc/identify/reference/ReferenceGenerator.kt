package jumpaku.fsc.identify.reference

import io.vavr.collection.Array
import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.arclength.ReparametrizedCurve
import jumpaku.core.curve.rationalbezier.ConicSection


interface ReferenceGenerator {

    fun <C: Curve> generate(
            fsc: ReparametrizedCurve<C>,
            t0: Double = fsc.originalCurve.domain.begin,
            t1: Double = fsc.originalCurve.domain.end): Reference

    companion object {

        fun reparametrize(conicSection: ConicSection): ReparametrizedCurve<ConicSection> = conicSection.let {
            val ts = Array.of(0.0, 0.3, 0.4, 0.47, 0.49, 0.5, 0.51, 0.53, 0.6, 0.7, 1.0)
            ReparametrizedCurve(it, ts)
        }

        fun ellipticDomain(
                s0: Double,
                s1: Double,
                base: ReparametrizedCurve<ConicSection>,
                complement: ReparametrizedCurve<ConicSection>): Interval {
            val lc = complement.chordLength
            val l = base.chordLength
            val L = l/(s1 - s0)
            val b = complement.reparametrizer.run { - toOriginal((L*s0/lc).coerceIn(range)) }
            val e = complement.reparametrizer.run { 2 - toOriginal((1 - (1 - s1)*L/lc).coerceIn(range)) }
            return Interval(b.coerceIn(Interval(-1.0, 2.0)), e.coerceIn(Interval(-1.0, 2.0)))
        }
    }
}
