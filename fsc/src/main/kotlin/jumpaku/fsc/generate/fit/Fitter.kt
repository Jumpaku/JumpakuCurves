package jumpaku.fsc.generate.fit

import jumpaku.core.curve.Curve
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.WeightedParamPoint


interface Fitter<out C : Curve> {

    fun fit(data: List<ParamPoint>, weights: List<Double> = data.map { 1.0 }): C {
        require(data.size == weights.size) { "data.size()(${data.size}) != weights.size()(${weights.size})" }

        return fit(data.zip(weights, ::WeightedParamPoint))
    }

    fun fit(data: List<WeightedParamPoint>): C
}