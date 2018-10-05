package jumpaku.fsc.snap.conicsection

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.collection.Array
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.geom.Point
import jumpaku.core.json.ToJson
import jumpaku.core.transform.Transform
import jumpaku.core.transform.toMatrixJson
import jumpaku.core.util.Option
import jumpaku.core.util.Result
import jumpaku.core.util.result
import jumpaku.core.util.toJson
import jumpaku.fsc.snap.point.PointSnapResult

data class ConicSectionSnapResult(val snappedConicSection: ConicSection, val candidates: Array<Candidate>) : ToJson {

    data class SnappedPoint(val source: Point, val target: Option<PointSnapResult>) : ToJson {

        override fun toString(): String = toJsonString()

        override fun toJson(): JsonElement = jsonObject(
                "source" to source.toJson(),
                "target" to target.map { it.toJson() }.toJson())

        companion object {

            fun fromJson(json: JsonElement): Result<SnappedPoint> = result {
                SnappedPoint(
                        Point.fromJson(json["source"]).orThrow(),
                        Option.fromJson(json["target"]).tryMap { it.map { PointSnapResult.fromJson(it).orThrow() } }.orThrow())
            }
        }
    }

    data class Candidate(
            val featurePoints: Array<SnappedPoint>,
            val transform: Transform) : ToJson {

        override fun toString(): String = toJsonString()

        override fun toJson(): JsonElement = jsonObject(
                "featurePoints" to jsonArray(featurePoints.map { it.toJson() }),
                "transform" to transform.toMatrixJson())

        companion object {

            fun fromJson(json: JsonElement): Result<Candidate> = result {
                Candidate(
                        Array.ofAll(json["featurePoints"].array.flatMap { SnappedPoint.fromJson(it).value() }),
                        Transform.fromMatrixJson(json["transform"]).orThrow())
            }
        }
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "snappedConicSection" to snappedConicSection.toJson(),
            "candidates" to jsonArray(candidates.map { it.toJson() }))

    companion object {

        fun fromJson(json: JsonElement): Result<ConicSectionSnapResult> = result {
            ConicSectionSnapResult(
                    ConicSection.fromJson(json["snappedConicSection"]).orThrow(),
                    json["candidates"].array.flatMap { Candidate.fromJson(it).value() }.let { Array.ofAll(it) })
        }
    }
}