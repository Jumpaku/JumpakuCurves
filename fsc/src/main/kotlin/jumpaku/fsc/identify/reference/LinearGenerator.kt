package jumpaku.fsc.identify.reference

import io.vavr.collection.Stream
import jumpaku.core.curve.Curve
import jumpaku.core.curve.Interval
import jumpaku.core.curve.arclength.ReparametrizedCurve
import jumpaku.core.curve.rationalbezier.ConicSection
import org.apache.commons.math3.analysis.solvers.BrentSolver

class LinearGenerator : ReferenceGenerator {

    override fun <C: Curve> generate(fsc: ReparametrizedCurve<C>, t0: Double, t1: Double): Reference {
        val s = fsc.originalCurve
        val base = reparametrize(ConicSection.lineSegment(s(t0), s(t1)))
        val s0 = fsc.reparametrizer.toArcLengthRatio(t0)
        val s1 = fsc.reparametrizer.toArcLengthRatio(t1)
        val c = base.originalCurve.complement().reverse()
        val l = base.originalCurve.begin.dist(c.end)
        val l0 = l*s0/(s1 - s0)
        val l1 = l*(1 - s1)/(s1 - s0)
        val solver = BrentSolver(1.0e-3)
        val b = Stream.iterate(0.5) { x -> x/2 }
                .find { c(0.5 + it).dist(c.end) > l0 }
                .map { solver.solve(50, { c(it).dist(c.end) - l0 }, 0.5 + it, 0.5 + it*2) }

        val e = Stream.iterate(0.5) { x -> x/2 }
                .find { c(0.5 - it).dist(c.begin) > l1 }
                .map { solver.solve(50, { c(it).dist(c.begin) - l1 }, 0.5 - it*2, 0.5 - it) }
        val domain = Interval(b.get() - 1, e.get() + 1)
        return Reference(base.originalCurve, domain)
    }


    fun <C: Curve> generateBeginEnd(fsc: ReparametrizedCurve<C>): Reference {
        val s = fsc.originalCurve
        val (t0, t1) = s.domain
        val base = ConicSection.lineSegment(s(t0), s(t1))
        return Reference(base, Interval.ZERO_ONE)
    }
}