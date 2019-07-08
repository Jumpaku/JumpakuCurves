package jumpaku.curves.fsc.blend

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.control.*
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.*
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.generate.DataPreparer
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.math.abs


class Overlap(
        val osm: OverlapMatrix,
        val grade: Grade,
        val path: List<Pair<Int, Int>>,
        val pairs: Set<Pair<Int, Int>>
) {
    fun isEmpty(): Boolean = path.isEmpty() && pairs.isEmpty()
}

class BlendData(
        val existFront: List<ParamPoint>,
        val existBack: List<ParamPoint>,
        val overlapFront: List<ParamPoint>,
        val overlapBack: List<ParamPoint>,
        val blended: List<WeightedParamPoint>) {

    fun data(): List<WeightedParamPoint> {
        return ((existFront + existBack + overlapFront + overlapBack).map { it.weighted(1.0) } + blended)
                .sortedBy { it.param }
    }

}

class BlendGenerator(
        val degree: Int = 3,
        val knotSpan: Double = 0.1,
        val dataPreparer: DataPreparer = DataPreparer(knotSpan / degree, knotSpan * 2, knotSpan * 2, 2),
        val fuzzifier: Fuzzifier = Fuzzifier.Linear(0.86, 0.77)
) {

    fun kernelDensityEstimate(paramPoints: List<WeightedParamPoint>, bandWidth: Double): List<WeightedParamPoint> {
        val n = paramPoints.size
        fun kernel(t: Double): Double = if (abs(t) > 1) 0.0 else 15 * (1 - t * t) * (1 - t * t) / 16
        fun density(t: Double): Double = paramPoints.sumByDouble { kernel((t - it.param) / bandWidth) } / n
        return paramPoints.map { it.run { copy(weight = weight / density(it.param)) } }
    }

    fun generate(blendData: BlendData): BSpline {
        val generator = Generator(degree, knotSpan, dataPreparer, fuzzifier)
        val samplingSpan = 0.01
        val data = blendData.data()
        val domain = Interval(data.first().param, data.last().param)
        val extended = dataPreparer.run {
            data.let { extendFront(it) }.let { extendBack(it) }.let { kernelDensityEstimate(it, samplingSpan) }
        }
        val extendedDomain = Interval(extended.first().param, extended.last().param)
        val removedKnots = LinkedList<Knot>()
        val knotVector = KnotVector.clamped(extendedDomain, degree, knotSpan).run {
            val front = blendData.existFront + blendData.overlapFront
            val back = blendData.existBack + blendData.overlapBack
            val blendBegin = blendData.blended.first().param
            val blendEnd = blendData.blended.last().param
            val remainedKnots = LinkedList<Knot>()
            fun shouldRemove(knot: Knot): Boolean =
                    (front.lastOrNull()?.let { knot.value in it.param..blendBegin } ?: false) ||
                            (back.firstOrNull()?.let { knot.value in blendEnd..it.param } ?: false)
            knots.forEach { knot ->
                if (shouldRemove(knot)) removedKnots.add(knot)
                else remainedKnots.add(knot)
            }
            KnotVector(degree, remainedKnots)
        }
        val s = generator.generate(extended, knotVector)
                .run { restrict(domain) }
        //.let { s -> removedKnots.fold(s) { inserted, (v, m) -> inserted.insertKnot(v, m) } }
        return s
    }
}

class Blender2(
        val samplingSpan: Double = 0.01,
        val blendingRate: Double = 0.65,
        val possibilityThreshold: Grade = Grade.FALSE) : ToJson {

    fun blend(existing: BSpline, overlapping: BSpline): Option<List<WeightedParamPoint>> {
        val existSamples = existing.sample(samplingSpan)
        val overlapSamples = overlapping.sample(samplingSpan)
        val osm = OverlapMatrix.create(existSamples.map { it.point }, overlapSamples.map { it.point })
        val overlap = detectOverlap(osm)
        return optionWhen(!overlap.isEmpty()) { resample(existSamples, overlapSamples, overlap).data() }
    }

    fun blend2(existing: BSpline, overlapping: BSpline): Option<BlendData> {
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
                    i == 0 -> (dpSearch(DpKey(i, j - 1)).map { it.extend(key, muij) } + DpValue(muij, 0, listOf(key)))
                            .maxWith(compare).toOption()
                    j == 0 -> (dpSearch(DpKey(i - 1, j)).map { it.extend(key, muij) } + DpValue(muij, 0, listOf(key)))
                            .maxWith(compare).toOption()
                    else -> listOf(DpKey(i - 1, j - 1), DpKey(i - 1, j), DpKey(i, j - 1))
                            .flatMap { dpSearch(it).map { value -> value.extend(key, muij) } }
                            .maxWith(compare).toOption()
                }
            }

            val right = (0 until osm.rowSize).map { DpKey(it, osm.columnLastIndex) }
            val bottom = (0 until osm.columnSize).map { DpKey(osm.rowLastIndex, it) }
            return (right + bottom).flatMap { dpSearch(it) }.maxWith(compare).toOption()
        }

        fun findLeftBottom(osm: OverlapMatrix, path: List<Pair<Int, Int>>, isAvailable: (i: Int, j: Int) -> Boolean): Option<DpValue> {
            if (path.isEmpty()) return none()
            val start = path.first()
            val goal = path.last()
            fun dpSearch(key: DpKey): Option<DpValue> = dpTable.getOrPut(key) {
                val (i, j) = key
                when {
                    !isAvailable(i, j) -> none()
                    key.asPair() == start -> some(DpValue(osm[i, j], 0, listOf(key)))
                    i == 0 -> dpSearch(DpKey(i, j - 1)).map { it.extend(key, osm[i, j]) }
                    j == 0 -> none()
                    else -> listOf(DpKey(i - 1, j), DpKey(i - 1, j - 1), DpKey(i, j - 1))
                            .flatMap { dpSearch(it).map { value -> value.extend(key, osm[i, j]) } }
                            .firstOrNull().toOption()
                }
            }
            return goal.let { (i, j) -> dpSearch(DpKey(i, j)) }
        }

        fun findRightAbove(osm: OverlapMatrix, path: List<Pair<Int, Int>>, isAvailable: (i: Int, j: Int) -> Boolean): Option<DpValue> {
            if (path.isEmpty()) return none()
            val start = path.first()
            val goal = path.last()
            fun dpSearch(key: DpKey): Option<DpValue> = dpTable.getOrPut(key) {
                val (i, j) = key
                when {
                    !isAvailable(i, j) -> none()
                    key.asPair() == start -> some(DpValue(osm[i, j], 0, listOf(key)))
                    i == 0 -> none()
                    j == 0 -> dpSearch(DpKey(i - 1, j)).map { it.extend(key, osm[i, j]) }
                    else -> listOf(DpKey(i, j - 1), DpKey(i - 1, j - 1), DpKey(i - 1, j))
                            .flatMap { dpSearch(it).map { value -> value.extend(key, osm[i, j]) } }
                            .firstOrNull().toOption()
                }
            }
            return goal.let { (i, j) -> dpSearch(DpKey(i, j)) }
        }
    }

    fun detectOverlap(osm: OverlapMatrix): Overlap {
        val threshold = possibilityThreshold
        val distMaxPath = PathFinder()
                .find(osm, compareBy({ it.dist }, { it.grade })) { i, j -> osm[i, j] > threshold }
                .map { it.subPath.map { it.asPair() } }
                .orDefault(emptyList())
        val available = collectPairs(osm, distMaxPath, threshold)
        val gradeOpt = PathFinder()
                .find(osm, compareBy({ it.grade }, { it.dist })) { i, j -> osm[i, j] > threshold && i to j in available }
                .map { it.grade }
        val gradeMaxPath = gradeOpt.flatMap { grade ->
            PathFinder()
                    .find(osm, compareBy { it.dist }) { i, j -> osm[i, j] >= grade && i to j in available }
                    .map { it.subPath.map { it.asPair() } }
        }.orDefault(emptyList())
        val pairs = collectPairs(osm, gradeMaxPath, threshold)
        return Overlap(osm, gradeOpt.orDefault(Grade.FALSE), gradeMaxPath, pairs)
    }

    fun resample(
            existing: List<ParamPoint>,
            overlapping: List<ParamPoint>,
            overlapInfo: Overlap
    ): BlendData {
        val blendedData = overlapInfo.pairs.map { (i, j) ->
            existing[i].lerp(blendingRate, overlapping[j]).weighted(overlapInfo.osm[i, j].value)
        }.sortedBy { it.param }
        val (pathBeginI, pathBeginJ) = overlapInfo.path.first()
        val (pathEndI, pathEndJ) = overlapInfo.path.last()
        val pathBeginExist = existing[pathBeginI].param
        val pathBeginOverlap = overlapping[pathBeginJ].param
        val pathEndExist = existing[pathEndI].param
        val pathEndOverlap = overlapping[pathEndJ].param
        val frontExistCount = (0 until pathBeginI).takeWhile { (it to 0) !in overlapInfo.pairs }.size
        val eFront = existing.take(frontExistCount)
                .map { it.run { copy(param = param + blendingRate * (pathBeginOverlap - pathBeginExist)) } }
        val backExistCount = (existing.lastIndex downTo (pathEndI + 1)).takeWhile { (it to overlapping.lastIndex) !in overlapInfo.pairs }.size
        val eBack = existing.takeLast(backExistCount)
                .map { it.run { copy(param = param + blendingRate * (pathEndOverlap - pathEndExist)) } }
        val frontOverlapCount = (0 until pathBeginJ).takeWhile { (0 to it) !in overlapInfo.pairs }.size
        val oFront = overlapping.take(frontOverlapCount)
                .map { it.run { copy(param = param - (1 - blendingRate) * (pathBeginOverlap - pathBeginExist)) } }
        val backOverlapCount = (overlapping.lastIndex downTo (pathEndJ + 1)).takeWhile { (existing.lastIndex to it) !in overlapInfo.pairs }.size
        val oBack = overlapping.takeLast(backOverlapCount)
                .map { it.run { copy(param = param - (1 - blendingRate) * (pathEndOverlap - pathEndExist)) } }
        return BlendData(eFront, eBack, oFront, oBack, blendedData)
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