package jumpaku.core.fit

import io.vavr.collection.Array
import jumpaku.core.curve.Curve
import jumpaku.core.curve.ParamPoint


interface Fitter<out C : Curve> {

    fun fit(data: Array<ParamPoint>, weights: Array<Double> = data.map { 1.0 }): C {
        require(data.size() == weights.size()) { "data.size()(${data.size()}) != weights.size()(${weights.size()})" }

        return fit(data.zipWith(weights, ::WeightedParamPoint))
    }

    fun fit(data: Array<WeightedParamPoint>): C
}