package jumpaku.curves.fsc.blend

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.control.Option
import jumpaku.commons.control.none
import jumpaku.commons.control.some
import jumpaku.commons.control.toOption
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.*
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import kotlin.math.abs


class Blender(
        val samplingSpan: Double = 0.01,
        val blendingRate: Double = 0.65,
        val possibilityThreshold: Grade = Grade.FALSE) : ToJson {

    init {
        require(samplingSpan > 0.0)
        require(blendingRate in 0.0..1.0)
    }

    fun blend(existing: BSpline, overlapping: BSpline): Option<List<WeightedParamPoint>> {
        val existSamples = existing.sample(samplingSpan)
        val overlapSamples = overlapping.sample(samplingSpan)
        val osm = OverlapMatrix.create(existSamples.map { it.point }, overlapSamples.map { it.point })
        return findPath(osm).map { resample(existSamples, overlapSamples, it, osm) }
    }

    fun findPath(osm: OverlapMatrix): Option<OverlapPath> {
        data class DpKey(val i: Int, val j: Int) {
            fun dist(key: DpKey): Int = key.let { abs(i - it.i) + abs(j - it.j) }
        }

        class DpValue(val dist: Int, val grade: Grade, val gradeSum: Double, val nodes: List<DpKey>) {
            fun extend(key: DpKey): DpValue {
                val d = dist + key.dist(nodes.last())
                val mu = grade and key.run { osm[i, j] }
                val sum = gradeSum + key.run { osm[i, j] }.value
                val l = nodes + key
                return DpValue(d, mu, sum, l)
            }
        }

        val compare = compareBy<DpValue>({ it.dist }, { it.grade }, { it.gradeSum })
        val dpTable = LinkedHashMap<DpKey, Option<DpValue>>(osm.rowSize * osm.columnSize)
        fun dpSearch(key: DpKey): Option<DpValue> = dpTable.getOrPut(key) {
            val (i, j) = key
            val muij = osm[i, j]
            when {
                muij <= possibilityThreshold -> none()
                i == 0 && j == 0 -> some(DpValue(0, muij, muij.value, listOf(key)))
                i == 0 -> (dpSearch(DpKey(i, j - 1)).map { it.extend(key) } + DpValue(0, muij, muij.value, listOf(key)))
                        .maxWith(compare).toOption()
                j == 0 -> (dpSearch(DpKey(i - 1, j)).map { it.extend(key) } + DpValue(0, muij, muij.value, listOf(key)))
                        .maxWith(compare).toOption()
                else -> listOf(DpKey(i - 1, j - 1), DpKey(i - 1, j), DpKey(i, j - 1))
                        .flatMap { dpSearch(it).map { value -> value.extend(key) } }
                        .maxWith(compare).toOption()
            }
        }

        val right = (0 until osm.rowSize).map { DpKey(it, osm.columnLastIndex) }
        val bottom = (0 until osm.columnSize).map { DpKey(osm.rowLastIndex, it) }
        return (right + bottom).flatMap { dpSearch(it) }.maxWith(compare).toOption().map { value ->
            val elements = value.nodes.map { e -> e.i to e.j }
            val type = OverlapType.judgeType(osm.rowSize, osm.columnSize, elements)
            OverlapPath(type, value.grade, elements)
        }
    }

    fun resample(
            existing: List<ParamPoint>,
            overlapping: List<ParamPoint>,
            path: OverlapPath,
            osm: OverlapMatrix
    ): List<WeightedParamPoint> {
        val (beginI, beginJ) = path.first()
        val (endI, endJ) = path.last()

        val blendedData = path.map { (i, j) -> existing[i].lerp(blendingRate, overlapping[j]).weighted(osm[i, j].value) }
        val (blendedBegin, blendedEnd) = blendedData.run { Interval(first().param, last().param) }

        fun List<ParamPoint>.weightedAll() = map { it.weighted(1.0) }
        fun front(data: List<ParamPoint>, nTake: Int) = rearrangeFront(data.take(nTake), blendedBegin).weightedAll()
        fun back(data: List<ParamPoint>, nDrop: Int) = rearrangeBack(data.drop(nDrop), blendedEnd).weightedAll()
        return when (path.type) {
            OverlapType.ExistOverlap -> front(existing, beginI) + blendedData + back(overlapping, endJ + 1)
            OverlapType.OverlapExist -> front(overlapping, beginJ) + blendedData + back(existing, endI + 1)
            OverlapType.ExistOverlapExist -> front(existing, beginI) + blendedData + back(existing, endI + 1)
            OverlapType.OverlapExistOverlap -> front(overlapping, beginJ) + blendedData + back(overlapping, endJ + 1)
        }
    }

    fun rearrangeFront(front: List<ParamPoint>, blendedBeginParam: Double): List<ParamPoint> =
            if (front.size <= 1) front.map { ParamPoint(it.point, blendedBeginParam - samplingSpan) }
            else {
                val fEnd = blendedBeginParam - samplingSpan
                val span = front.run { last().param - first().param }
                val fBegin = (fEnd - span).coerceAtMost(fEnd)
                transformParams(front, range = Interval(fBegin, fEnd))
            }

    fun rearrangeBack(back: List<ParamPoint>, blendedEndParam: Double): List<ParamPoint> =
            if (back.size <= 1) back.map { ParamPoint(it.point, blendedEndParam + samplingSpan) }
            else {
                val bBegin = blendedEndParam + samplingSpan
                val span = back.run { last().param - first().param }
                val bEnd = (bBegin + span).coerceAtLeast(bBegin)
                transformParams(back, range = Interval(bBegin, bEnd))
            }

    override fun toJson(): JsonElement = jsonObject(
            "samplingSpan" to samplingSpan.toJson(),
            "blendingRate" to blendingRate.toJson(),
            "possibilityThreshold" to possibilityThreshold.toJson())

    override fun toString(): String = toJsonString()

    companion object {

        fun fromJson(json: JsonElement): Blender = Blender(
                json["samplingSpan"].double,
                json["blendingRate"].double,
                Grade.fromJson(json["possibilityThreshold"].asJsonPrimitive))
    }
}