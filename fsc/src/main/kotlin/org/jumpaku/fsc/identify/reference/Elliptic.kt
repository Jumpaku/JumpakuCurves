package org.jumpaku.fsc.identify.reference

import io.vavr.API.Tuple
import io.vavr.API.Array
import org.apache.commons.math3.analysis.solvers.BrentSolver
import org.apache.commons.math3.optim.MaxEval
import org.apache.commons.math3.optim.MaxIter
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import org.apache.commons.math3.optim.univariate.BrentOptimizer
import org.apache.commons.math3.optim.univariate.SearchInterval
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction
import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.FuzzyCurve
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.IntervalJson
import org.jumpaku.core.curve.rationalbezier.ConicSection
import org.jumpaku.core.curve.rationalbezier.ConicSectionJson
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2
import org.jumpaku.core.json.prettyGson


class Elliptic(val conicSection: ConicSection, val domain: Interval) : Reference {

    override val fuzzyCurve: FuzzyCurve = object : FuzzyCurve {

        override val domain: Interval = this@Elliptic.domain

        override fun evaluate(t: Double): Point {
            require(t in domain) { "t($t) is out of domain($domain)" }
            return evaluateWithoutDomain(t, conicSection)
        }
    }

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): EllipticJson = EllipticJson(this)

    companion object {

        /**
         * Computes a far point.
         * A point far is a far point if and only if the point maximizes a triangle area(fsc(t0), far, fsc(t1)).
         * A line segment(m, far), where m is middle point of fsc(t0) and fsc(t1), bisects a area surrounded by an elliptic arc(fsc(t0), fsc(t1)) and line segment(fsc(t0), fsc(t1)).
         */
        private fun triangleAreaMaximizingFar(t0: Double, t1: Double, fsc: FuzzyCurve): Double {
            val middle = fsc(t0).divide(0.5, fsc(t1)).toCrisp()
            val ts = Interval(t0, t1).sample(100)
            val ps = ts.map(fsc).map(Point::toCrisp)
            val areas = ps.zipWith(ps.tail(), middle::area)
                    .foldLeft(Array(0.0), { arr, area -> arr.append(arr.last() + area) })
            val index = areas.lastIndexWhere { it < areas.last()/2 }

            val relative = 1.0e-8
            val absolute = 1.0e-5
            return BrentSolver(relative, absolute).solve(50, {
                val m = fsc(it).toCrisp()
                val l = areas[index] + middle.area(ps[index], m)
                val r = areas.last() - areas[index + 1] + middle.area(ps[index + 1], m)
                l - r
            }, ts[index], ts[index + 1])
        }

        private fun possibilityMaximizingWeight(t0: Double, t1: Double, tf: Double, fsc: FuzzyCurve): Double {
            val begin = fsc(t0)
            val end = fsc(t1)
            val far = fsc(tf)
            val relative = 1.0e-8
            val absolute = 1.0e-5
            val brent = BrentOptimizer(relative, absolute)

            val positiveWeight = brent
                    .optimize(MaxEval(50), MaxIter(50), SearchInterval(-0.999, 0.999, 0.5), GoalType.MAXIMIZE,
                            UnivariateObjectiveFunction {
                                val elliptic = ConicSection(begin, far, end, it)
                                val domain = createDomain(t0, t1, fsc.toArcLengthCurve(), elliptic)
                                Elliptic(elliptic, domain).isValidFor(fsc).value
                            })
            val negativeWeight = brent
                    .optimize(MaxEval(50), MaxIter(50), SearchInterval(-0.999, 0.999, -0.5), GoalType.MAXIMIZE,
                            UnivariateObjectiveFunction {
                                val elliptic = ConicSection(begin, far, end, it)
                                val domain = createDomain(t0, t1, fsc.toArcLengthCurve(), elliptic)
                                Elliptic(elliptic, domain).isValidFor(fsc).value
                            })
            return maxOf(positiveWeight, negativeWeight, compareBy { it.value }).point
        }

        private fun possibilityMaximizingFar(t0: Double, t1: Double, fsc: FuzzyCurve): Double {
            val begin = fsc(t0)
            val end = fsc(t1)

            return Interval(t0, t1).sample(100).map {
                val elliptic = ConicSection(begin, fsc(it), end)
                val domain = createDomain(t0, t1, fsc.toArcLengthCurve(), elliptic)
                val reference = Elliptic(elliptic, domain)
                val mu = reference.fuzzyCurve.toArcLengthCurve().evaluateAll(100).zipWith(fsc.toArcLengthCurve().evaluateAll(100), {
                    a, b -> 1 - a.toCrisp().dist(b.toCrisp()) / (a.r + b.r)
                }).min().get()
                Tuple(it, mu)
            } .maxBy { (_, mu) -> mu } .get()._1()
        }

        private fun ellipticConicSection(t0: Double, t1: Double, fsc: FuzzyCurve): ConicSection {
            val tf = possibilityMaximizingFar(t0, t1, fsc)

            return ConicSection(fsc(t0), fsc(tf), fsc(t1))
        }

        fun create(t0: Double, t1: Double, fsc: FuzzyCurve): Elliptic {
            val elliptic = ellipticConicSection(t0, t1, fsc)
            val domain = createDomain(t0, t1, fsc.toArcLengthCurve(), elliptic)

            return Elliptic(elliptic, domain)
        }
    }
}

data class EllipticJson(val conicSection: ConicSectionJson, val domain: IntervalJson){

    constructor(elliptic: Elliptic) : this(elliptic.conicSection.json(), elliptic.domain.json())

    fun elliptic(): Elliptic = Elliptic(conicSection.conicSection(), domain.interval())
}
