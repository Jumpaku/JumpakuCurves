package jumpaku.curves.fsc.identify.primitive.reference

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.Sampler
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.geom.Point


interface ReferenceGenerator {

    fun <C : Curve> generate(
        fsc: ReparametrizedCurve<C>,
        t0: Double = fsc.originalCurve.domain.begin,
        t1: Double = fsc.originalCurve.domain.end
    ): Reference

    companion object {

        fun ellipticDomain(
            s0: Double,
            s1: Double,
            base: ReparametrizedCurve<ConicSection>,
            complement: ReparametrizedCurve<ConicSection>,
            nSamples: Int
        ): Interval {
            val lc = complement(Sampler(nSamples)).zipWithNext(Point::dist).sum()
            val l = base(Sampler(nSamples)).zipWithNext(Point::dist).sum()
            val L = l / (s1 - s0)
            val b = -complement.toOriginal((L * s0 / lc).coerceIn(Interval.Unit))
            val e = 2 - complement.toOriginal((1 - (1 - s1) * L / lc).coerceIn(Interval.Unit))
            return Interval(b.coerceIn(Interval(-1.0, 2.0)), e.coerceIn(Interval(-1.0, 2.0)))
        }
    }
}

