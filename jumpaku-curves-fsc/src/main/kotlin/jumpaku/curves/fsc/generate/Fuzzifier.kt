package jumpaku.curves.fsc.generate

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
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

object FuzzifierJson : JsonConverterBase<Fuzzifier>() {

    override fun toJson(src: Fuzzifier): JsonElement = when (src) {
        is Fuzzifier.Linear -> src.run {
            jsonObject(
                    "type" to "Linear",
                    "velocityCoefficient" to velocityCoefficient,
                    "accelerationCoefficient" to accelerationCoefficient)
        }
    }

    override fun fromJson(json: JsonElement): Fuzzifier = when (json["type"].string) {
        "Linear" -> Fuzzifier.Linear(json["velocityCoefficient"].double, json["accelerationCoefficient"].double)
        else -> error("invalid fuzzifier type: ${json["type"].string}")
    }
}
