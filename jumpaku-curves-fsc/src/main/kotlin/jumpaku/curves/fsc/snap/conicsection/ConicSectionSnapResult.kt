package jumpaku.curves.fsc.snap.conicsection

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.control.Option
import jumpaku.commons.json.JsonConverterBase
import jumpaku.commons.option.json.OptionJson
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.curve.bezier.ConicSectionJson
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.fuzzy.GradeJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.PointJson
import jumpaku.curves.core.transform.Transform
import jumpaku.curves.core.transform.TransformJson
import jumpaku.curves.fsc.snap.point.PointSnapResult
import jumpaku.curves.fsc.snap.point.PointSnapResultJson

class ConicSectionSnapResult(val snappedConicSection: Option<ConicSection>, candidates: Iterable<EvaluatedCandidate>) {

    data class SnappedPoint(val source: Point, val target: Option<PointSnapResult>)

    class Candidate(featurePoints: Iterable<SnappedPoint>, val transform: Transform) {

        val featurePoints: List<SnappedPoint> = featurePoints.toList()
    }

    data class EvaluatedCandidate(val grade: Grade, val candidate: Candidate)

    val candidates: List<EvaluatedCandidate> = candidates.toList()
}

object ConicSectionSnapResultJson : JsonConverterBase<ConicSectionSnapResult>() {

    override fun toJson(src: ConicSectionSnapResult): JsonElement = src.run {
        jsonObject(
                "snappedConicSection" to OptionJson.toJson(snappedConicSection.map { ConicSectionJson.toJson(it) }),
                "candidates" to jsonArray(candidates.map { EvaluatedCandidateJson.toJson(it) }))
    }

    override fun fromJson(json: JsonElement): ConicSectionSnapResult = ConicSectionSnapResult(
            OptionJson.fromJson(json["snappedConicSection"]).map { ConicSectionJson.fromJson(it) },
            json["candidates"].array.map { EvaluatedCandidateJson.fromJson(it) })

    object SnappedPointJson : JsonConverterBase<ConicSectionSnapResult.SnappedPoint>() {
        override fun toJson(src: ConicSectionSnapResult.SnappedPoint): JsonElement = src.run {
            jsonObject(
                    "source" to PointJson.toJson(source),
                    "target" to OptionJson.toJson(target.map { PointSnapResultJson.toJson(it) }))
        }

        override fun fromJson(json: JsonElement): ConicSectionSnapResult.SnappedPoint = ConicSectionSnapResult.SnappedPoint(
                PointJson.fromJson(json["source"]),
                OptionJson.fromJson(json["target"]).map { PointSnapResultJson.fromJson(it) })
    }

    object CandidateJson : JsonConverterBase<ConicSectionSnapResult.Candidate>() {

        override fun toJson(src: ConicSectionSnapResult.Candidate): JsonElement = src.run {
            jsonObject(
                    "featurePoints" to jsonArray(featurePoints.map { SnappedPointJson.toJson(it) }),
                    "transform" to TransformJson.toJson(transform))
        }

        override fun fromJson(json: JsonElement): ConicSectionSnapResult.Candidate =
                ConicSectionSnapResult.Candidate(
                        json["featurePoints"].array.map { SnappedPointJson.fromJson(it) },
                        TransformJson.fromJson(json["transform"]))
    }
    object EvaluatedCandidateJson : JsonConverterBase<ConicSectionSnapResult.EvaluatedCandidate>() {

        override fun toJson(src: ConicSectionSnapResult.EvaluatedCandidate): JsonElement = src.run {
            jsonObject("grade" to GradeJson.toJson(grade),
                    "candidate" to CandidateJson.toJson(candidate))
        }

        override fun fromJson(json: JsonElement): ConicSectionSnapResult.EvaluatedCandidate = ConicSectionSnapResult.EvaluatedCandidate(
                GradeJson.fromJson(json["grade"].asJsonPrimitive),
                CandidateJson.fromJson(json["candidate"]))
    }
}