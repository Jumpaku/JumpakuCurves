package jumpaku.curves.fsc.generate.fit

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.WeightedParamPoint


interface Fitter<out C : Curve> {

    fun fit(data: List<ParamPoint>, weights: List<Double> = data.map { 1.0 }): C {
        require(data.size == weights.size) { "data.size()(${data.size}) != weights.size()(${weights.size})" }
        require(data.size >= 2) { "data.size == ${data.size}, too few data" }

        return fit(data.zip(weights, ::WeightedParamPoint))
    }

    fun fit(data: List<WeightedParamPoint>): C
}