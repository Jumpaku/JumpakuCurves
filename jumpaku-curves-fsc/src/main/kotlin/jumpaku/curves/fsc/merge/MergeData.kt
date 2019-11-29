package jumpaku.curves.fsc.merge

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.commons.control.*
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.generate.fit.WeightedParamPoint
import jumpaku.curves.fsc.generate.fit.weighted

class MergeData(
        val grade: Grade,
        val front: List<ParamPoint>,
        val back: List<ParamPoint>,
        val merged: List<WeightedParamPoint>) : ToJson {

    val aggregated: List<WeightedParamPoint> = ((front + back).map { it.weighted(1.0) } + merged)
            .sortedBy { it.param }

    val domain: Interval = aggregated.run { Interval(first().param, last().param) }

    val mergeInterval: Interval = merged.run { Interval(first().param, last().param) }

    val frontInterval: Option<Interval> =
            front.takeIf { it.isNotEmpty() }?.run { Interval(first().param, last().param) }.toOption()

    val backInterval: Option<Interval> =
            back.takeIf { it.isNotEmpty() }?.run { Interval(first().param, last().param) }.toOption()

    override fun toJson(): JsonElement = jsonObject(
            "grade" to grade.toJson(),
            "front" to front.map { it.toJson() }.toJsonArray(),
            "back" to back.map { it.toJson() }.toJsonArray(),
            "merged" to merged.map { it.toJson() }.toJsonArray())

    override fun toString(): String = toJsonString()

    companion object {

        fun parameterize(
                existing: List<ParamPoint>,
                overlapping: List<ParamPoint>,
                mergeRate: Double,
                overlapState: OverlapState
        ): MergeData {
            require(existing.size == overlapState.osm.rowSize)
            require(overlapping.size == overlapState.osm.columnSize)
            require(mergeRate in 0.0..1.0)

            val mergedData = overlapState.range
                    .map { (i, j) -> existing[i].lerp(mergeRate, overlapping[j]).weighted(overlapState.osm[i, j].value) }
                    .sortedBy { it.param }

            val (ridgeBeginI, ridgeBeginJ) = overlapState.ridge.first()
            val ridgeBeginExist = existing[ridgeBeginI].param
            val ridgeBeginOverlap = overlapping[ridgeBeginJ].param
            val frontExistCount = (0 until ridgeBeginI)
                    .takeWhile { (it to 0) !in overlapState.range }.size
            val eFront = existing.take(frontExistCount)
                    .map { it.run { copy(param = param + mergeRate * (ridgeBeginOverlap - ridgeBeginExist)) } }
            val frontOverlapCount = (0 until ridgeBeginJ)
                    .takeWhile { (0 to it) !in overlapState.range }.size
            val oFront = overlapping.take(frontOverlapCount)
                    .map { it.run { copy(param = param - (1 - mergeRate) * (ridgeBeginOverlap - ridgeBeginExist)) } }

            val (ridgeEndI, ridgeEndJ) = overlapState.ridge.last()
            val ridgeEndExist = existing[ridgeEndI].param
            val ridgeEndOverlap = overlapping[ridgeEndJ].param
            val backExistCount = (existing.lastIndex downTo (ridgeEndI + 1))
                    .takeWhile { (it to overlapping.lastIndex) !in overlapState.range }.size
            val eBack = existing.takeLast(backExistCount)
                    .map { it.run { copy(param = param + mergeRate * (ridgeEndOverlap - ridgeEndExist)) } }
            val backOverlapCount = (overlapping.lastIndex downTo (ridgeEndJ + 1))
                    .takeWhile { (existing.lastIndex to it) !in overlapState.range }.size
            val oBack = overlapping.takeLast(backOverlapCount)
                    .map { it.run { copy(param = param - (1 - mergeRate) * (ridgeEndOverlap - ridgeEndExist)) } }

            return MergeData(overlapState.grade, eFront + oFront, eBack + oBack, mergedData)
        }

        fun fromJson(json: JsonElement): MergeData = MergeData(
                Grade.fromJson(json["grade"].asJsonPrimitive),
                json["front"].array.map { ParamPoint.fromJson(it) },
                json["back"].array.map { ParamPoint.fromJson(it) },
                json["merged"].array.map { WeightedParamPoint.fromJson(it) })
    }
}
