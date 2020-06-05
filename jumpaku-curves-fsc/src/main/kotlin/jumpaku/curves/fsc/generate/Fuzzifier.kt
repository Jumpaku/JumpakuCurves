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
            val d1 = crisp.derivative
            val d2 = d1.derivative
            return ts.map { velocityCoefficient * d1(it).length() + accelerationCoefficient * d2(it).length() }
        }
    }
}

