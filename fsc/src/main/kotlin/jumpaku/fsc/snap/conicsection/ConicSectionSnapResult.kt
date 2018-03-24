package jumpaku.fsc.snap.conicsection

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.collection.Array
import io.vavr.collection.Stream
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.affine.Affine
import jumpaku.core.affine.Point
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.fuzzy.Grade
import jumpaku.core.json.ToJson
import jumpaku.fsc.snap.point.PointSnapResult

data class ConicSectionSnapResult(
        val candidate: Candidate,
        val grade: Grade,
        val candidates: Stream<Candidate>) : ToJson {

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "candidate" to candidate.toJson(),
            "grade" to grade.toJson(),
            "candidates" to jsonArray(candidates.map { it.toJson() }))

    data class SnapPointPair(
            val cursor: Point,
            val snapped: PointSnapResult) : ToJson {

        override fun toString(): String = toJsonString()

        override fun toJson(): JsonElement = jsonObject(
                "cursor" to cursor.toJson(),
                "snapped" to snapped.toJson())

        companion object {

            fun fromJson(json: JsonElement): Option<SnapPointPair> = Try.ofSupplier {
                SnapPointPair(
                        Point.fromJson(json["cursor"]).get(),
                        PointSnapResult.fromJson(json["snapped"]).get())
            }.toOption()
        }
    }

    data class Candidate(
            val featurePoints: Array<SnapPointPair>,
            val snapTransform: Affine,
            val snappedConicSection: ConicSection) : ToJson {

        override fun toString(): String = toJsonString()

        override fun toJson(): JsonElement = jsonObject(
                "featurePoints" to jsonArray(featurePoints.map { it.toJson() }),
                "snapTransform" to snapTransform.toJson(),
                "snappedConicSection" to snappedConicSection.toJson())

        companion object {

            fun fromJson(json: JsonElement): Option<Candidate> = Try.ofSupplier {
                Candidate(
                        Array.ofAll(json["featurePoints"].array.flatMap { SnapPointPair.fromJson(it) }),
                        Affine.fromJson(json["snapTransform"]).get(),
                        ConicSection.fromJson(json["snappedConicSection"]).get())
            }.toOption()
        }
    }

    companion object {

        fun fromJson(json: JsonElement): Option<ConicSectionSnapResult> = Try.ofSupplier {
            ConicSectionSnapResult(
                    Candidate.fromJson(json["candidate"]).get(),
                    Grade.fromJson(json["grade"].asJsonPrimitive).get(),
                    Stream.ofAll(json["candidates"].array.flatMap { Candidate.fromJson(it) }))
        }.toOption()
    }
}