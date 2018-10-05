package jumpaku.core.transform

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.core.geom.Vector
import jumpaku.core.json.ToJson
import jumpaku.core.util.Result
import jumpaku.core.util.result
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.util.FastMath

class Rotate(val axis: Vector, val angleRadian: Double): Transform, ToJson {

    constructor(from: Vector, to: Vector, angleRadian: Double = from.angle(to)): this(from.cross(to), angleRadian)

    override val matrix: RealMatrix get() {
        val (x, y, z) = axis.normalize().orThrow()
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

        fun fromJson(json: JsonElement): Result<Rotate> = result {
            Rotate(Vector.fromJson(json["axis"]).orThrow(), json["angleRadian"].double)
        }
    }
}

