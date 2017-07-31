package org.jumpaku.fsc.identify.reference

import io.vavr.API
import io.vavr.API.Array
import io.vavr.Tuple3
import org.apache.commons.math3.analysis.solvers.BrentSolver
import org.apache.commons.math3.optim.*
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import org.apache.commons.math3.optim.univariate.BrentOptimizer
import org.apache.commons.math3.optim.univariate.SearchInterval
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction
import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.FuzzyCurve
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.IntervalJson
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.rationalbezier.ConicSection
import org.jumpaku.core.curve.rationalbezier.ConicSectionJson
import org.jumpaku.core.json.prettyGson
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2
import org.jumpaku.core.util.component3


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
         * Far point on the fsc is a point such that line segment(f, m) bisects a area surrounded by an elliptic arc(fsc(t0), fsc(t1)) and a line segment(fsc(t0), fsc(t1)),
         * where f is far point, m is the middle point between fsc(t0) and fsc(t1).
         */
        private fun triangleAreaBisectingFar(t0: Double, t1: Double, fsc: FuzzyCurve): Double {
            val middle = fsc(t0).middle(fsc(t1))
            val ts = Interval(t0, t1).sample(100)
            val ps = ts.map(fsc)
            val areas = ps.zipWith(ps.tail(), middle::area)
                    .foldLeft(Array(0.0), { arr, area -> arr.append(arr.last() + area) })
            val index = areas.lastIndexWhere { it < areas.last()/2 }

            val relative = 1.0e-7
            val absolute = 1.0e-4
            return BrentSolver(relative, absolute).solve(50, {
                val m = fsc(it)
                val l = areas[index] + middle.area(ps[index], m)
                val r = areas.last() - areas[index + 1] + middle.area(ps[index + 1], m)
                l - r
            }, ts[index], ts[index + 1])
        }

        private fun possibilityMaximizingWeight(t0: Double, t1: Double, tf: Double, fsc: FuzzyCurve): Double {
            val begin = fsc(t0)
            val end = fsc(t1)
            val far = fsc(tf)
            val fscArcLength = fsc.toArcLengthCurve()
            val fmpsFsc = fscArcLength.evaluateAll(30)

            val relative = 1.0e-7
            val absolute = 1.0e-4
            val possibilityF =  { w: Double ->
                val elliptic = ConicSection(begin, far, end, w)
                val domain = createDomain(t0, t1, fscArcLength, elliptic)
                val reference = Elliptic(elliptic, domain)

                reference.fuzzyCurve.toArcLengthCurve().evaluateAll(30).zipWith(fmpsFsc, {
                    a, b -> 1 - a.dist(b) / (a.r + b.r)
                }).min().get()
            }

            return BrentOptimizer(relative, absolute)
                    .optimize(
                            MaxEval(50),
                            MaxIter(50),
                            SearchInterval(-0.999, 0.999),
                            GoalType.MAXIMIZE,
                            UnivariateObjectiveFunction(possibilityF)
                    ).point
        }

        fun triangleAreaMaximizingParams(fsc: BSpline, nSamples: Int = 99): Tuple3<Double, Double, Double> {
            val ts = fsc.domain.sample(nSamples)
            return API.For(ts.take(nSamples/3), ts.drop(2*nSamples/3))
                    .yield({ t0, t1 ->
                        val tf = Elliptic.triangleAreaBisectingFar(t0, t1, fsc)
                        API.Tuple(API.Tuple(t0, tf, t1), fsc(tf).area(fsc(t0), fsc(t1)))
                    })
                    .maxBy { (_, area) -> area }
                    .map { it._1() }.get()
        }

        fun ofParams(t0: Double, t1: Double, fsc: BSpline): Elliptic {
            val tf = triangleAreaBisectingFar(t0, t1, fsc)
            val w = possibilityMaximizingWeight(t0, t1, tf, fsc)
            val conicSection = ConicSection(fsc(t0), fsc(tf), fsc(t1), w)
            val domain = createDomain(t0, t1, fsc.toArcLengthCurve(), conicSection)

            return Elliptic(conicSection, domain)
        }

        fun ofBeginEnd(fsc: BSpline): Elliptic {
            return ofParams(fsc.domain.begin, fsc.domain.end, fsc)
        }

        fun of(fsc: BSpline): Elliptic {
            val (t0, _, t1) = triangleAreaMaximizingParams(fsc)

            return ofParams(t0, t1, fsc)
        }
    }
}

data class EllipticJson(val conicSection: ConicSectionJson, val domain: IntervalJson){

    constructor(elliptic: Elliptic) : this(elliptic.conicSection.json(), elliptic.domain.json())

    fun elliptic(): Elliptic = Elliptic(conicSection.conicSection(), domain.interval())
}
