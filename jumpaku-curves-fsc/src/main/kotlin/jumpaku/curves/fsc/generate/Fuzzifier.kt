package jumpaku.curves.fsc.generate

import jumpaku.curves.core.curve.bspline.BSpline

sealed class Fuzzifier {

    abstract fun fuzzify(crisp: BSpline, ts: List<Double>): List<Double>

    class Linear(val velocityCoefficient: Double, val accelerationCoefficient: Double) : Fuzzifier() {

        init {
            require(velocityCoefficient >= 0.0)
            require(accelerationCoefficient >= 0.0)
        }

        override fun fuzzify(crisp: BSpline, ts: List<Double>): List<Double> {
            val d1 = crisp.differentiate()
            val d2 = d1.differentiate()
            val v = d1.invoke(ts)
            val a = d2.invoke(ts)
            val r = ts.indices.map {
                velocityCoefficient * v[it].length() + accelerationCoefficient * a[it].length()
            }
            return r
        }
    }
}

