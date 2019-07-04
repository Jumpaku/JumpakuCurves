package jumpaku.curves.fsc.blend

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.control.*
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.WeightedParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.weighted
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.lerp
import jumpaku.curves.core.geom.middle
import kotlin.math.abs
import kotlin.math.absoluteValue


class Overlap(
        val osm: OverlapMatrix,
        val grade: Grade,
        val path: List<Pair<Int, Int>>,
        val pairs: Set<Pair<Int, Int>>
) {
    fun isEmpty(): Boolean = path.isEmpty() && pairs.isEmpty()
}

class Blender2(
        val samplingSpan: Double = 0.01,
        val blendingRate: Double = 0.65,
        val possibilityThreshold: Grade = Grade.FALSE,
        val bandWidth: Double = samplingSpan*1.5) : ToJson {

    fun blend(existing: BSpline, overlapping: BSpline): Option<List<WeightedParamPoint>> {
        val existSamples = existing.sample(samplingSpan)
        val overlapSamples = overlapping.sample(samplingSpan)
        val osm = OverlapMatrix.create(existSamples.map { it.point }, overlapSamples.map { it.point })
        val overlap = detectOverlap(osm)
        return optionWhen(!overlap.isEmpty()) { resample(existSamples, overlapSamples, overlap) }
    }

    fun collectPairs(osm: OverlapMatrix, path: List<Pair<Int, Int>>, possibilityThreshold: Grade): Set<Pair<Int, Int>> {
        if (path.isEmpty()) return emptySet()

        val q = mutableSetOf<Pair<Int, Int>>()
        path.forEach { (i, j) ->
            q += (i downTo 0).takeWhile { osm[it, j] > possibilityThreshold }.map { it to j }
            q += (i..osm.rowLastIndex).takeWhile { osm[it, j] > possibilityThreshold }.map { it to j }
            q += (j downTo 0).takeWhile { osm[i, it] > possibilityThreshold }.map { i to it }
            q += (j..osm.columnLastIndex).takeWhile { osm[i, it] > possibilityThreshold }.map { i to it }
        }
        return q
    }

    class PathFinder {

        data class DpKey(val i: Int, val j: Int) {
            fun asPair(): Pair<Int, Int> = i to j
        }

        class DpValue(val grade: Grade, val dist: Int, val subPath: List<DpKey>) {
            fun extend(key: DpKey, keyGrade: Grade): DpValue {
                val extended = subPath + key
                val (fi, fj) = extended.first()
                val (li, lj) = extended.last()
                return DpValue(grade and keyGrade, abs(li - fi) + abs(lj - fj), extended)
            }
        }

        val dpTable = LinkedHashMap<DpKey, Option<DpValue>>()

        fun find(osm: OverlapMatrix, compare: Comparator<DpValue>, isAvailable: (i: Int, j: Int) -> Boolean): Option<DpValue> {
            fun dpSearch(key: DpKey): Option<DpValue> = dpTable.getOrPut(key) {
                val (i, j) = key
                val muij = osm[i, j]
                when {
                    !isAvailable(i, j) -> none()
                    i == 0 && j == 0 -> some(DpValue(muij, 0, listOf(key)))
                    i == 0 -> (dpSearch(DpKey(i, j - 1)).map { it.extend(key, osm[i, j]) } + DpValue(muij, 0, listOf(key)))
                            .maxWith(compare).toOption()
                    j == 0 -> (dpSearch(DpKey(i - 1, j)).map { it.extend(key, osm[i, j]) } + DpValue(muij, 0, listOf(key)))
                            .maxWith(compare).toOption()
                    else -> listOf(DpKey(i - 1, j - 1), DpKey(i - 1, j), DpKey(i, j - 1))
                            .flatMap { dpSearch(it).map { value -> value.extend(key, osm[i, j]) } }
                            .maxWith(compare).toOption()
                }
            }
            val right = (0 until osm.rowSize).map { DpKey(it, osm.columnLastIndex) }
            val bottom = (0 until osm.columnSize).map { DpKey(osm.rowLastIndex, it) }
            return (right + bottom).flatMap { dpSearch(it) }.maxWith(compare).toOption()
        }

        fun findLeftBottom(osm: OverlapMatrix, path: List<Pair<Int, Int>>, isAvailable: (i: Int, j: Int) -> Boolean): Option<DpValue> {
            if (path.isEmpty()) return none()
            fun dpSearch(key: DpKey): Option<DpValue> = dpTable.getOrPut(key) {
                val (i, j) = key
                when {
                    !isAvailable(i, j) -> none()
                    key.asPair() == path.first() -> some(DpValue(osm[i, j], 0, listOf(key)))
                    i == 0 -> dpSearch(DpKey(i, j - 1)).map { it.extend(key, osm[i, j]) }
                    j == 0 -> none()
                    else -> listOf(DpKey(i - 1, j), DpKey(i - 1, j - 1), DpKey(i, j - 1))
                            .flatMap { dpSearch(it).map { value -> value.extend(key, osm[i, j]) } }
                            .firstOrNull().toOption()
                }
            }
            return path.last().let { (i, j) -> dpSearch(DpKey(i, j)) }
        }
        fun findRightAbove(osm: OverlapMatrix, path: List<Pair<Int, Int>>, isAvailable: (i: Int, j: Int) -> Boolean): Option<DpValue> {
            if (path.isEmpty()) return none()
            fun dpSearch(key: DpKey): Option<DpValue> = dpTable.getOrPut(key) {
                val (i, j) = key
                when {
                    !isAvailable(i, j) -> none()
                    key.asPair() == path.first() -> some(DpValue(osm[i, j], 0, listOf(key)))
                    i == 0 -> none()
                    j == 0 -> dpSearch(DpKey(i, j - 1)).map { it.extend(key, osm[i, j]) }
                    else -> listOf( DpKey(i, j - 1), DpKey(i - 1, j - 1), DpKey(i - 1, j))
                            .flatMap { dpSearch(it).map { value -> value.extend(key, osm[i, j]) } }
                            .firstOrNull().toOption()
                }
            }
            return path.last().let { (i, j) -> dpSearch(DpKey(i, j)) }
        }
    }

    fun detectOverlap(osm: OverlapMatrix): Overlap {
        val threshold = possibilityThreshold
        val distMaxPath = PathFinder()
                .find(osm, compareBy({ it.dist }, { it.grade })) { i, j -> osm[i, j] > threshold }
                .map { it.subPath.map { it.asPair() } }
                .orDefault(emptyList())
        val available = collectPairs(osm, distMaxPath, threshold)
        val (grade, gradeMaxPath) = PathFinder()
                .find(osm, compareBy({ it.grade }, { it.dist })) { i, j -> osm[i, j] > threshold && i to j in available }
                .map { it.grade to it.subPath.map { it.asPair() } }
                .orDefault(Grade.FALSE to emptyList())
        val pairs = collectPairs(osm, gradeMaxPath, threshold)
        return Overlap(osm, grade, gradeMaxPath, pairs)
    }

    fun resample(
            existing: List<ParamPoint>,
            overlapping: List<ParamPoint>,
            overlapInfo: Overlap
    ): List<WeightedParamPoint> {
        val blendedData = overlapInfo.pairs.map { (i, j) ->
            existing[i].lerp(blendingRate, overlapping[j]).weighted(overlapInfo.osm[i, j].value)
        }.sortedBy { it.param }
        println("bd : ${blendedData.first().param} ${blendedData.last().param}")
        val rearranged = rearrangeParams(
                overlapInfo.path.first(), overlapInfo.path.last(), existing, overlapping, overlapInfo)

        return (rearranged.flatMap { it.map { it.weighted(1.0) } } + blendedData).sortedBy { it.param }
                //.let { kernelDensityEstimate(it) }
    }

    fun rearrangeParams(
            pathBegin: Pair<Int, Int>,
            pathEnd: Pair<Int, Int>,
            existing: List<ParamPoint>,
            overlapping: List<ParamPoint>,
            overlapInfo: Overlap
    ): List<List<ParamPoint>> {
        val (beginI, beginJ) = pathBegin
        val (endI, endJ) = pathEnd
        val eBegin = existing[beginI].param
        val eEnd = existing[endI].param
        val oBegin = overlapping[beginJ].param
        val oEnd = overlapping[endJ].param
        val q = overlapInfo.pairs
        val eFront = (0 until beginI)
                .takeWhile { it to 0 !in q }
                .map { existing[it].run { copy(param = param + blendingRate * (oBegin - eBegin)) } }
        val eBack = (existing.lastIndex downTo (endI + 1))
                .takeWhile { it to overlapping.lastIndex !in q }
                .reversed()
                .map { existing[it].run { copy(param = param + blendingRate * (oEnd - eEnd)) } }
        val oFront = (0 until beginJ)
                .takeWhile { 0 to it !in q }
                .map { overlapping[it].run { copy(param = param - (1 - blendingRate) * (oBegin - eBegin)) } }
        val oBack = (overlapping.lastIndex downTo (endJ + 1))
                .takeWhile { existing.lastIndex to it !in q }
                .reversed()
                .map { overlapping[it].run { copy(param = param - (1 - blendingRate) * (oEnd - eEnd)) } }
        if (eFront.isNotEmpty()) println("${eFront.first().param} ${eFront.last().param}")
        if (oFront.isNotEmpty()) println("${oFront.first().param} ${oFront.last().param}")
        println("--${eBegin.lerp(blendingRate, oBegin)} ${eEnd.lerp(blendingRate, oEnd)}")
        if (eBack.isNotEmpty()) println("${eBack.first().param} ${eBack.last().param}")
        if (oBack.isNotEmpty()) println("${oBack.first().param} ${oBack.last().param}")
        println()
        return listOf(eFront, eBack, oFront, oBack)
    }

    /*fun kernelDensityEstimate(paramPoints: List<WeightedParamPoint>): List<WeightedParamPoint> {
        val n = paramPoints.size
        fun kernel(t: Double): Double = if (abs(t) > 1) 0.0 else 15*(1-t*t)*(1-t*t)/16
        fun density(t: Double): Double = paramPoints.sumByDouble { kernel((t - it.param)/bandWidth) }/n
        return paramPoints.map { it.run { copy(weight = weight/density(it.param)) } }
    }*/

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