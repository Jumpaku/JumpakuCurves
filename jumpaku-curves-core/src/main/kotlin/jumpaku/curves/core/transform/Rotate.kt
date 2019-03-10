package jumpaku.curves.core.transform

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.geom.Vector
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.util.FastMath

class Rotate(val axis: Vector, val angleRadian: Double) : Transform, ToJson {

    init {
        require(axis.run { div(length()).isSuccess }) { "axis($axis) is close to zero" }
    }

    override val matrix: RealMatrix
        get() {
            val (x, y, z) = axis.normalize().value().orNull() ?: return Transform.Identity.matrix
            val cos = FastMath.cos(angleRadian)
            val sin = FastMath.sin(angleRadian)
            return MatrixUtils.createRealMatrix(arrayOf(
                    doubleArrayOf(x * x * (1 - cos) + cos, x * y * (1 - cos) - z * sin, z * x * (1 - cos) + y * sin, 0.0),
                    doubleArrayOf(x * y * (1 - cos) + z * sin, y * y * (1 - cos) + cos, y * z * (1 - cos) - x * sin, 0.0),
                    doubleArrayOf(z * x * (1 - cos) - y * sin, y * z * (1 - cos) + x * sin, z * z * (1 - cos) + cos, 0.0),
                    doubleArrayOf(0.0, 0.0, 0.0, 1.0)
            ))
        }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "axis" to axis.toJson(),
            "angleRadian" to angleRadian.toJson())

    companion object {

        fun fromJson(json: JsonElement): Rotate = Rotate(Vector.fromJson(json["axis"]), json["angleRadian"].double)

        /**
         * @throws IllegalArgumentException when from is close to -to
         */
        fun of(from: Vector, to: Vector, angleRadian: Double = from.angle(to)): Rotate {
            val cross = from.cross(to)
            val dot = from.dot(to)
            require(cross.normalize().isSuccess || dot >= 0) { "from($from) is close to -to$($to)" }
            return if (cross.normalize().isFailure) Rotate(Vector.K, 0.0)
            else Rotate(cross, angleRadian)
        }
    }
}

