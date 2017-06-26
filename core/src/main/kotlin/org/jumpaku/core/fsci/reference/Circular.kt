package org.jumpaku.core.fsci.reference

import org.apache.commons.math3.analysis.solvers.BrentSolver
import org.apache.commons.math3.util.Precision
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.divide
import org.jumpaku.core.curve.FuzzyCurve
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.rationalbezier.ConicSection
import org.jumpaku.core.fuzzy.Grade


class Circular(val conicSection: ConicSection, val domain: Interval) : Reference {

    override val fuzzyCurve: FuzzyCurve get() = object : FuzzyCurve {

        override val domain: Interval get() = this@Circular.domain

        override fun evaluate(t: Double): Point {
            require(t in domain) { "t($t) is out of domain($domain)" }
            return evaluateWithoutDomain(t, conicSection)
        }
    }

    companion object {

        private fun circularConicSection(t0: Double, t1: Double, fsc: FuzzyCurve): ConicSection {
            val begin = fsc(t0)
            val end = fsc(t1)
            val relative = 1.0e-8
            val absolute = 1.0e-5
            val tf = BrentSolver(relative, absolute).solve(50, {
                val f = fsc(it).toCrisp()
                f.distSquare(begin.toCrisp()) - (f.distSquare(end.toCrisp()))
            }, t0, t1, t0.divide(0.5, t1))
            val far = fsc(tf)
            val m = begin.divide(0.5, end)
            val ll = m.toCrisp().distSquare(begin.toCrisp())
            val hh = m.toCrisp().distSquare(far.toCrisp())

            return ConicSection(begin, far, end, (ll - hh)/(ll + hh))
        }

        fun create(t0: Double, t1: Double, fsc: FuzzyCurve): Circular {
            val circular = circularConicSection(t0, t1, fsc)
            val domain = createDomain(t0, t1, fsc.toArcLengthCurve(), circular)

            return Circular(circular, domain)
        }
    }
}
