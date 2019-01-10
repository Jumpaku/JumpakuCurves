package jumpaku.fsc.snap

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.core.geom.*
import jumpaku.core.transform.Rotate
import jumpaku.core.transform.Transform
import jumpaku.core.transform.Translate
import jumpaku.core.transform.UniformlyScale
import jumpaku.core.json.ToJson
import jumpaku.core.util.Result
import jumpaku.core.util.result
import org.apache.commons.math3.util.FastMath

open class Grid(
        val baseSpacing: Double,
        val baseFuzziness: Double = 0.0,
        val magnification: Int = 2,
        val origin: Point = Point(0.0, 0.0, 0.0, 0.0),
        val rotation: Rotate = Rotate(Vector.K, 0.0)): ToJson {

    open fun spacing(resolution: Int): Double = baseSpacing * FastMath.pow(magnification.toDouble(), -resolution)

    open fun fuzziness(resolution: Int): Double = baseFuzziness * FastMath.pow(magnification.toDouble(), -resolution)

    /**
     * localToWorld transforms coordinates in local(grid) to coordinates in world.
     * Coordinates in world is transformed by the following transformations;
     *  scaling by spacing,
     *  translation to specified origin.
     */
    fun localToWorld(resolution: Int): Transform = rotation
            .andThen(UniformlyScale(spacing(resolution)))
            .andThen(Translate(origin - Point.origin))

    fun snapToNearestGrid(cursor: Point, resolution: Int): GridPoint = localToWorld(resolution).invert().orThrow()(cursor)
            .let { (x, y, z) -> GridPoint(FastMath.round(x), FastMath.round(y), FastMath.round(z)) }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "baseSpacing" to baseSpacing.toJson(),
            "magnification" to magnification.toJson(),
            "origin" to origin.toJson(),
            "rotation" to rotation.toJson(),
            "baseFuzziness" to baseFuzziness.toJson())

    companion object {

        fun fromJson(json: JsonElement): Result<Grid> = result { Grid(
                baseSpacing = json["baseSpacing"].double,
                magnification = json["magnification"].int,
                origin = Point.fromJson(json["origin"]),
                rotation = Rotate.fromJson(json["rotation"]),
                baseFuzziness = json["baseFuzziness"].double)
        }
    }
}
