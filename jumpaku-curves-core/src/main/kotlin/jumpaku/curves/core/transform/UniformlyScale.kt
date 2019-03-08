package jumpaku.curves.core.transform

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix


class UniformlyScale(val scale: Double = 1.0) : Transform, ToJson {

    override val matrix: RealMatrix = MatrixUtils.createRealDiagonalMatrix(doubleArrayOf(scale, scale, scale, 1.0))

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("scale" to scale.toJson())

    companion object {

        fun fromJson(json: JsonElement): UniformlyScale = UniformlyScale(json["scale"].double)
    }
}
