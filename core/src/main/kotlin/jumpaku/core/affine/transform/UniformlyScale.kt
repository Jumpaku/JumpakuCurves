package jumpaku.core.affine.transform

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.json.ToJson
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix


class UniformlyScale(val scale: Double = 1.0) : Transform, ToJson {

    override val matrix: RealMatrix = MatrixUtils.createRealDiagonalMatrix(doubleArrayOf(scale, scale, scale, 1.0))

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("scale" to scale.toJson())

    companion object {

        fun fromJson(json: JsonElement): Option<UniformlyScale> = Try.ofSupplier {
            UniformlyScale(json["scale"].double)
        }.toOption()
    }
}
