package jumpaku.fsc.snap.conicsection

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.fuzzy.Grade
import jumpaku.core.geom.Point
import jumpaku.core.json.ToJson
import jumpaku.core.transform.Transform
import jumpaku.core.transform.toMatrixJson
import jumpaku.core.util.Option
import jumpaku.core.util.Result
import jumpaku.core.util.result
import jumpaku.core.util.toJson
import jumpaku.fsc.snap.point.PointSnapResult

class ConicSectionSnapResult(val snappedConicSection: Option<ConicSection>, candidates: Iterable<EvaluatedCandidate>) : ToJson {

    data class SnappedPoint(val source: Point, val target: Option<PointSnapResult>) : ToJson {

        override fun toString(): String = toJsonString()

        override fun toJson(): JsonElement = jsonObject(
                "source" to source.toJson(),
                "target" to target.map { it.toJson() }.toJson())

        companion object {

            fun fromJson(json: JsonElement): Result<SnappedPoint> = result {
                SnappedPoint(
                        Point.fromJson(json["source"]).orThrow(),
                        Option.fromJson(json["target"]).tryMap {
                            it.flatMap { PointSnapResult.fromJson(it).value() }
                        }.orThrow())
            }
        }
    }

    class Candidate(featurePoints: Iterable<SnappedPoint>, val transform: Transform) : ToJson {

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

    data class EvaluatedCandidate(val grade: Grade, val candidate: Candidate): ToJson {

        override fun toString(): String = toJsonString()

        override fun toJson(): JsonElement =
                jsonObject("grade" to grade.toJson(), "candidate" to candidate.toJson())

        companion object {

            fun fromJson(json: JsonElement): Result<EvaluatedCandidate> = result {
                EvaluatedCandidate(
                        Grade.fromJson(json["grade"].asJsonPrimitive).orThrow(),
                        Candidate.fromJson(json["candidate"]).orThrow())
            }
        }
    }

    val candidates: List<EvaluatedCandidate> = candidates.toList()

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "snappedConicSection" to snappedConicSection.map { it.toJson() }.toJson(),
            "candidates" to jsonArray(candidates.map { it.toJson() }))

    companion object {

        fun fromJson(json: JsonElement): Result<ConicSectionSnapResult> = result {
            ConicSectionSnapResult(
                    Option.fromJson(json["snappedConicSection"]).orThrow().flatMap { ConicSection.fromJson(it).value() },
                    json["candidates"].array.flatMap { EvaluatedCandidate.fromJson(it).value() })
        }
    }
}