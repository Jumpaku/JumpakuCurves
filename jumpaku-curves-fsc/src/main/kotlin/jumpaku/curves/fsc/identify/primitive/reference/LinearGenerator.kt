package jumpaku.curves.fsc.identify.primitive.reference

import jumpaku.commons.control.toOption
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.fsc.identify.primitive.reparametrize
import org.apache.commons.math3.analysis.solvers.BrentSolver

class LinearGenerator : ReferenceGenerator {

    override fun <C : Curve> generate(fsc: ReparametrizedCurve<C>, t0: Double, t1: Double): Reference {
        val s = fsc.originalCurve
        val base = reparametrize(ConicSection.lineSegment(s(t0), s(t1)))
        val s0 = fsc.reparametrizer.toArcLengthRatio(t0)
        val s1 = fsc.reparametrizer.toArcLengthRatio(t1)
        val c = base.originalCurve.complement().reverse()
        val l = base.originalCurve.begin.dist(c.end)
        val l0 = l * s0 / (s1 - s0)
        val l1 = l * (1 - s1) / (s1 - s0)
        val solver = BrentSolver(1.0e-3)
        val b = generateSequence(0.5) { x -> x / 2 }
                .find { c(0.5 + it).dist(c.end) > l0 }
                .toOption()
                .map { solver.solve(50, { c(it).dist(c.end) - l0 }, 0.5 + it, 0.5 + it * 2) }
                .orThrow()
        val e = generateSequence(0.5) { x -> x / 2 }
                .find { c(0.5 - it).dist(c.begin) > l1 }
                .toOption()
                .map { solver.solve(50, { c(it).dist(c.begin) - l1 }, 0.5 - it * 2, 0.5 - it) }
                .orThrow()
        val domain = Interval(b - 1, e + 1)
        return Reference(base.originalCurve, domain)
    }

    fun <C : Curve> generateBeginEnd(fsc: ReparametrizedCurve<C>): Reference {
        val s = fsc.originalCurve
        val (t0, t1) = s.domain
        val base = ConicSection.lineSegment(s(t0), s(t1))
        return Reference(base, Interval.ZERO_ONE)
    }
}