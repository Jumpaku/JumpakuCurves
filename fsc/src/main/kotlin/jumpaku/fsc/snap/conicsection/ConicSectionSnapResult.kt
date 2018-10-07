package jumpaku.fsc.snap.conicsection

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
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

class ConicSectionSnapResult(val snappedConicSection: ConicSection, candidates: Iterable<Candidate>) : ToJson {

    class SnappedPoint(val source: Point, val target: Option<PointSnapResult>) : ToJson {

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

    class Candidate(
            featurePoints: Iterable<SnappedPoint>,
            val transform: Transform) : ToJson {

        val featurePoints: List<SnappedPoint> = featurePoints.toList()

        override fun toString(): String = toJsonString()

        override fun toJson(): JsonElement = jsonObject(
                "featurePoints" to jsonArray(featurePoints.map { it.toJson() }),
                "transform" to transform.toMatrixJson())

        companion object {

            fun fromJson(json: JsonElement): Result<Candidate> = result {
                Candidate(
                        json["featurePoints"].array.flatMap { SnappedPoint.fromJson(it).value() },
                        Transform.fromMatrixJson(json["transform"]).orThrow())
            }
        }
    }

    val candidates: List<Candidate> = candidates.toList()

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "snappedConicSection" to snappedConicSection.toJson(),
            "candidates" to jsonArray(candidates.map { it.toJson() }))

    companion object {

        fun fromJson(json: JsonElement): Result<ConicSectionSnapResult> = result {
            ConicSectionSnapResult(
                    ConicSection.fromJson(json["snappedConicSection"]).orThrow(),
                    json["candidates"].array.flatMap { Candidate.fromJson(it).value() })
        }
    }
}