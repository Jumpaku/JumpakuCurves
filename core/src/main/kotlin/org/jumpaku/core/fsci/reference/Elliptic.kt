package org.jumpaku.core.fsci.reference

import org.apache.commons.math3.optim.MaxEval
import org.apache.commons.math3.optim.MaxIter
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import org.apache.commons.math3.optim.univariate.BrentOptimizer
import org.apache.commons.math3.optim.univariate.SearchInterval
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.divide
import org.jumpaku.core.curve.FuzzyCurve
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.rationalbezier.ConicSection


class Elliptic(val conicSection: ConicSection, val domain: Interval) : Reference {

    override val fuzzyCurve: FuzzyCurve get() = object : FuzzyCurve {

        override val domain: Interval get() = this@Elliptic.domain

        override fun evaluate(t: Double): Point {
            require(t in domain) { "t($t) is out of domain($domain)" }
            return evaluateWithoutDomain(t, conicSection)
        }
    }

    companion object {

        private fun triangleAreaMaximizedFar(t0: Double, t1: Double, fsc: FuzzyCurve): Point {
            val begin = fsc(t0)
            val end = fsc(t1)
            val relative = 1.0e-8
            val absolute = 1.0e-5
            val tf = BrentOptimizer(relative, absolute)
                    .optimize(MaxEval(50), MaxIter(50), SearchInterval(t0, t1, t0.divide(0.5, t1)), GoalType.MAXIMIZE,
                            UnivariateObjectiveFunction {
                                fsc(it).toCrisp().area(begin.toCrisp(), end.toCrisp())
                            }).point
            return fsc(tf)
        }

        private fun possibilityMaximizedWeight(t0: Double, t1: Double, far: Point, fsc: FuzzyCurve): Double {
            val begin = fsc(t0)
            val end = fsc(t1)
            val relative = 1.0e-8
            val absolute = 1.0e-5
            val brent = BrentOptimizer(relative, absolute)
            val positiveWeight = brent
                    .optimize(MaxEval(50), MaxIter(50), SearchInterval(-0.999, 0.999, 0.5), GoalType.MAXIMIZE,
                            UnivariateObjectiveFunction {
                                val elliptic = ConicSection(begin, far, end, it)
                                val domain = createDomain(t0, t1, fsc.toArcLengthCurve(), elliptic)
                                Elliptic(elliptic, domain).validate(fsc).value
                            })
            val negativeWeight = brent
                    .optimize(MaxEval(50), MaxIter(50), SearchInterval(-0.999, 0.999, -0.5), GoalType.MAXIMIZE,
                            UnivariateObjectiveFunction {
                                val elliptic = ConicSection(begin, far, end, it)
                                val domain = createDomain(t0, t1, fsc.toArcLengthCurve(), elliptic)
                                Elliptic(elliptic, domain).validate(fsc).value
                            })
            return maxOf(positiveWeight, negativeWeight, compareBy { it.value }).point
        }

        fun create(t0: Double, t1: Double, fsc: FuzzyCurve): Elliptic {
            val far = triangleAreaMaximizedFar(t0, t1, fsc)
            val weight = possibilityMaximizedWeight(t0, t1, far, fsc)
            val elliptic = ConicSection(fsc(t0), far, fsc(t1), weight)
            val domain = createDomain(t0, t1, fsc.toArcLengthCurve(), elliptic)

            return Elliptic(elliptic.reverse(), domain)
        }
    }
}
