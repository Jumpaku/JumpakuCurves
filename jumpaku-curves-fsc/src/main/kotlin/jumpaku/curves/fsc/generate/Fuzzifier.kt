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
            val v = ts.map { d1.evaluate(it) }
            val a = ts.map { d2.evaluate(it) }
            return ts.mapIndexed { i, t ->
                velocityCoefficient * v[i].length() + accelerationCoefficient * a[i].length()
            }
        }
    }
}

