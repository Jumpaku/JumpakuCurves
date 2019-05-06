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
        val blendedData = path.map { (i, j) -> existing[i].lerp(blendingRate, overlapping[j]).weighted(osm[i, j].value) }
        val rearranged = rearrangeParams(path.first(), path.last(), existing, overlapping)
        return (rearranged.flatMap { it.map { it.weighted(1.0) } } + blendedData).sortedBy { it.param }
    }

    private fun rearrangeParams(pathBegin: Pair<Int, Int>, pathEnd: Pair<Int, Int>, existing: List<ParamPoint>, overlapping: List<ParamPoint>): List<List<ParamPoint>> {
        val (beginI, beginJ) = pathBegin
        val (endI, endJ) = pathEnd
        val eBegin = existing[beginI].param
        val eEnd = existing[endI].param
        val oBegin = overlapping[beginJ].param
        val oEnd = overlapping[endJ].param
        val eFront = existing.take(beginI).map { it.copy(param = it.param + blendingRate * (oBegin - eBegin)) }
        val eBack = existing.drop(endI + 1).map { it.copy(param = it.param + blendingRate * (oEnd - eEnd)) }
        val oFront = overlapping.take(beginJ).map { it.copy(param = it.param - (1 - blendingRate) * (oBegin - eBegin)) }
        val oBack = overlapping.drop(endJ + 1).map { it.copy(param = it.param - (1 - blendingRate) * (oEnd - eEnd)) }
        return listOf(eFront, eBack, oFront, oBack)
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

/*class Blender2(val blender: Blender) {

    fun blend(existing: BSpline, overlapping: BSpline): Option<List<WeightedParamPoint>> {
        val existSamples = existing.sample(blender.samplingSpan)
        val overlapSamples = overlapping.sample(blender.samplingSpan)
        val osm = OverlapMatrix.create(existSamples.map { it.point }, overlapSamples.map { it.point })
        val path = blender.findPath(osm).orNull() ?: return none()
        val pairs = collectPairs(osm, path)
        return some(resample(existSamples, overlapSamples, pairs, osm))
    }

    fun collectPairs(osm: OverlapMatrix, path: OverlapPath): OverlapPairList {
        val q = mutableSetOf<Pair<Int, Int>>()
        path.forEach { (i, j) ->
            q += (i downTo 0).takeWhile { osm[it, j] > blender.possibilityThreshold }.map { it to j }
            q += (i..osm.rowLastIndex).takeWhile { osm[it, j] > blender.possibilityThreshold }.map { it to j }
            q += (j downTo 0).takeWhile { osm[i, it] > blender.possibilityThreshold }.map { i to it }
            q += (j..osm.columnLastIndex).takeWhile { osm[i, it] > blender.possibilityThreshold }.map { i to it }
        }
        return OverlapPairList(path, q.sortedBy { it.first + it.second })
    }

    fun resample(
            existing: List<ParamPoint>,
            overlapping: List<ParamPoint>,
            pairs: OverlapPairList,
            osm: OverlapMatrix
    ): List<WeightedParamPoint> {

        val blendedData = pairs.map { (i, j) -> existing[i].lerp(blender.blendingRate, overlapping[j]).weighted(osm[i, j].value) }
        val (blendedBegin, blendedEnd) = blendedData.run { Interval(first().param, last().param) }

        fun List<ParamPoint>.weightedAll() = map { it.weighted(1.0) }
        fun front(data: List<ParamPoint>, nTake: Int) = blender.rearrangeFront(data.take(nTake), blendedBegin).weightedAll()
        fun back(data: List<ParamPoint>, nDrop: Int) = blender.rearrangeBack(data.drop(nDrop), blendedEnd).weightedAll()
        val (beginI, beginJ) = pairs.path.first()
        val (endI, endJ) = pairs.path.last()
        return when (pairs.path.type) {
            OverlapType.ExistOverlap -> front(existing, beginI) + blendedData + back(overlapping, endJ + 1)
            OverlapType.OverlapExist -> front(overlapping, beginJ) + blendedData + back(existing, endI + 1)
            OverlapType.ExistOverlapExist -> front(existing, beginI) + blendedData + back(existing, endI + 1)
            OverlapType.OverlapExistOverlap -> front(overlapping, beginJ) + blendedData + back(overlapping, endJ + 1)
        }
    }
}*/