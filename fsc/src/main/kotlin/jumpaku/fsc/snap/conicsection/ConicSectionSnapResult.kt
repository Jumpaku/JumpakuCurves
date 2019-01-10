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

            fun fromJson(json: JsonElement): SnappedPoint = SnappedPoint(
                    Point.fromJson(json["source"]),
                    Option.fromJson(json["target"]) { PointSnapResult.fromJson(it) })
        }
    }

    class Candidate(featurePoints: Iterable<SnappedPoint>, val transform: Transform) : ToJson {

        val featurePoints: List<SnappedPoint> = featurePoints.toList()

        override fun toString(): String = toJsonString()

        override fun toJson(): JsonElement = jsonObject(
                "featurePoints" to jsonArray(featurePoints.map { it.toJson() }),
                "transform" to transform.toMatrixJson())

        companion object {

            fun fromJson(json: JsonElement): Candidate =
                Candidate(
                        json["featurePoints"].array.map { SnappedPoint.fromJson(it) },
                        Transform.fromMatrixJson(json["transform"]))
        }
    }

    data class EvaluatedCandidate(val grade: Grade, val candidate: Candidate): ToJson {

        override fun toString(): String = toJsonString()

        override fun toJson(): JsonElement =
                jsonObject("grade" to grade.toJson(), "candidate" to candidate.toJson())

        companion object {

            fun fromJson(json: JsonElement): EvaluatedCandidate = EvaluatedCandidate(
                    Grade.fromJson(json["grade"].asJsonPrimitive),
                    Candidate.fromJson(json["candidate"]))
        }
    }

    val candidates: List<EvaluatedCandidate> = candidates.toList()

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "snappedConicSection" to snappedConicSection.map { it.toJson() }.toJson(),
            "candidates" to jsonArray(candidates.map { it.toJson() }))

    companion object {

        fun fromJson(json: JsonElement): ConicSectionSnapResult = ConicSectionSnapResult(
                Option.fromJson(json["snappedConicSection"]).map { ConicSection.fromJson(it) },
                json["candidates"].array.map { EvaluatedCandidate.fromJson(it) })
    }
}