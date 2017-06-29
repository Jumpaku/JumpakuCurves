package org.jumpaku.fsc.identify.reference

import org.apache.commons.math3.analysis.solvers.BrentSolver
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.divide
import org.jumpaku.core.curve.FuzzyCurve
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.IntervalJson
import org.jumpaku.core.curve.rationalbezier.ConicSection
import org.jumpaku.core.curve.rationalbezier.ConicSectionJson
import org.jumpaku.core.json.prettyGson


class Circular(val conicSection: ConicSection, val domain: Interval) : Reference {

    override val fuzzyCurve: FuzzyCurve = object : FuzzyCurve {

        override val domain: Interval = this@Circular.domain

        override fun evaluate(t: Double): Point {
            require(t in domain) { "t($t) is out of domain($domain)" }
            return evaluateWithoutDomain(t, conicSection)
        }
    }

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): CircularJson = CircularJson(this)

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


data class CircularJson(val conicSection: ConicSectionJson, val domain: IntervalJson){

    constructor(circular: Circular) : this(circular.conicSection.json(), circular.domain.json())

    fun circular(): Circular = Circular(conicSection.conicSection(), domain.interval())
}