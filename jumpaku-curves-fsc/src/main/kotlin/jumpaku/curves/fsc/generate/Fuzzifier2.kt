package jumpaku.curves.fsc.generate

import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline2.BSpline
import jumpaku.curves.core.curve.bspline2.Nurbs
import jumpaku.curves.core.geom.weighted

sealed class Fuzzifier2 {

    abstract fun fuzzify(crisp: BSpline, ts: List<Double>): List<Double>

    class Linear(val velocityCoefficient: Double, val accelerationCoefficient: Double) : Fuzzifier2() {

        init {
            require(velocityCoefficient >= 0.0)
            require(accelerationCoefficient >= 0.0)
        }

        override fun fuzzify(crisp: BSpline, ts: List<Double>): List<Double> {
            val d1 = crisp.differentiate()
            val d2 = d1.differentiate()
            val v = d1.evaluateAll(ts)
            val a = d2.evaluateAll(ts)
            val r = ts.indices.map {
                velocityCoefficient * v[it].length() + accelerationCoefficient * a[it].length()
            }
            return r
        }
    }
}

