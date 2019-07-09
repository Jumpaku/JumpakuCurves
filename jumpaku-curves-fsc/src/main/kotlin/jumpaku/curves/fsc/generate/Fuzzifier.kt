package jumpaku.curves.fsc.generate

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.bspline.BSpline

sealed class Fuzzifier : ToJson {

    abstract fun fuzzify(crisp: BSpline, ts: List<Double>): List<Double>

    override fun toString(): String = toJsonString()

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

        override fun toJson(): JsonElement = jsonObject(
                "type" to "Linear",
                "velocityCoefficient" to velocityCoefficient,
                "accelerationCoefficient" to accelerationCoefficient)
    }

    companion object {

        fun fromJson(json: JsonElement): Fuzzifier = when (json["type"].string) {
            "Linear" -> Linear(json["velocityCoefficient"].double, json["accelerationCoefficient"].double)
            else -> error("invalid fuzzifier type: ${json["type"].string}")
        }
    }
}