package jumpaku.fsc.snap.conicsection

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.collection.Array
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.fuzzy.Grade
import jumpaku.core.geom.Point
import jumpaku.core.json.ToJson
import jumpaku.core.json.jsonOption
import jumpaku.core.json.option
import jumpaku.core.transform.Transform
import jumpaku.core.transform.toMatrixJson
import jumpaku.fsc.snap.point.PointSnapResult

data class ConicSectionSnapResult(val snappedConicSection: ConicSection, val candidates: Array<Candidate>) : ToJson {

    data class SnappedPoint(val source: Point, val target: Option<PointSnapResult>) : ToJson {

        override fun toString(): String = toJsonString()

        override fun toJson(): JsonElement = jsonObject(
                "source" to source.toJson(),
                "target" to jsonOption(target.map { it.toJson() }))

        companion object {

            fun fromJson(json: JsonElement): Option<SnappedPoint> = Try.ofSupplier {
                SnappedPoint(
                        Point.fromJson(json["source"]).get(),
                        json["target"].option.flatMap { PointSnapResult.fromJson(it) })
            }.toOption()
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

            fun fromJson(json: JsonElement): Option<Candidate> = Try.ofSupplier {
                Candidate(
                        Array.ofAll(json["featurePoints"].array.flatMap { SnappedPoint.fromJson(it) }),
                        Transform.fromMatrixJson(json["transform"]).get())
            }.toOption()
        }
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "snappedConicSection" to snappedConicSection.toJson(),
            "candidates" to jsonArray(candidates.map { it.toJson() }))

    companion object {

        fun fromJson(json: JsonElement): Option<ConicSectionSnapResult> = Try.ofSupplier {
            ConicSectionSnapResult(
                    ConicSection.fromJson(json["snappedConicSection"]).get(),
                    json["candidates"].array.flatMap { Candidate.fromJson(it) }.let { Array.ofAll(it) })
        }.toOption()
    }
}