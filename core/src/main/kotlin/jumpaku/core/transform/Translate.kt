package jumpaku.core.transform

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.geom.Vector
import jumpaku.core.json.ToJson
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix


class Translate(val move: Vector = Vector()): Transform, ToJson {

    constructor(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) : this(Vector(x, y, z))

    override val matrix: RealMatrix = MatrixUtils.createRealMatrix(arrayOf(
            doubleArrayOf(1.0, 0.0, 0.0, move.x),
            doubleArrayOf(0.0, 1.0, 0.0, move.y),
            doubleArrayOf(0.0, 0.0, 1.0, move.z),
            doubleArrayOf(0.0, 0.0, 0.0, 1.0)
    ))

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("move" to move.toJson())

    companion object {

        fun fromJson(json: JsonElement): Option<Translate> = Try.ofSupplier {
            Translate(Vector.fromJson(json["move"]).get())
        }.toOption()
    }
}
