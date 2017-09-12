package jumpaku.fsc.blend

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.HashMap
import io.vavr.collection.Stream
import io.vavr.control.Option
import jumpaku.core.affine.divide
import jumpaku.core.curve.Interval
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.fit.transformParams
import jumpaku.core.fuzzy.grade
import jumpaku.core.json.ToJson
import jumpaku.core.json.jsonOption
import jumpaku.core.json.option
import jumpaku.core.util.*
import jumpaku.fsc.generate.FscGenerator


data class BlendResult(
        val osm: OverlappingMatrix,
        val path: Option<OverlappingPath>,
        val blended: Option<BSpline>): ToJson{

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement {
        val osmJson = jsonArray(osm.matrix.map { jsonArray(it.map { it.toJson() }) })
        val pathJson = jsonOption(path.map { (_, grade, path) -> jsonObject(
                "grade" to grade.toJson(),
                "pairs" to jsonArray(path.map { (i, j) ->
                    jsonObject("i" to i.toJson(), "j" to j.toJson())
                }))
        })
        val blendedJson = jsonOption(blended.map { it.toJson() })
        return jsonObject(
                "osm" to osmJson,
                "path" to pathJson,
                "blended" to blendedJson)
    }
}

val JsonElement.blendResult: BlendResult get() {
    val osm = OverlappingMatrix(Array.ofAll(this["osm"].array.map { Array.ofAll(it.array.map { it.grade }) }))
    val path = this["path"].option.map { OverlappingPath(
            osm,
            it["grade"].grade,
            Array.ofAll(it["pairs"].array.map { Tuple2(it["i"].int, it["j"].int) }))
    }
    val blended = this["blended"].option.map { it.bSpline }

    return BlendResult(osm, path, blended)
}

class Blender(
        val samplingSpan: Double = 1.0/128,
        val blendingRate: Double = 0.5,
        val fscGenerator: FscGenerator = FscGenerator(),
        val evaluatePath: OverlappingPath.(OverlappingMatrix) -> Double = { _ -> grade.value }) {

    fun blend(existing: BSpline, overlapping: BSpline): BlendResult {
        val osm = OverlappingMatrix(samplingSpan, existing, overlapping)
        val path = searchPath(osm)
        return BlendResult(osm, path, path.map {
            fscGenerator.generate(resample(existing, overlapping, it))
        })
    }

    fun searchPath(osm: OverlappingMatrix): Option<OverlappingPath> {

        var dpTable = HashMap.empty<Tuple2<Int, Int>, OverlappingPath>()
        fun subPath(i: Int, j: Int): OverlappingPath {
            val path =  dpTable[Tuple2(i, j)].getOrElse {
                val uij = osm[i, j]
                when {
                    i == 0 || j == 0 -> osm.initialPath(uij, i, j)
                    else -> Array.of(subPath(i - 1, j), subPath(i, j - 1), subPath(i - 1, j - 1))
                            .maxBy { (_, grade, _) -> grade }
                            .map { it.extend(uij, i, j) }
                            .get()
                }
            }
            dpTable = dpTable.put(Tuple2(i, j), path)
            return path
        }
        return Stream.concat(
                (0..osm.rowLastIndex).map { i ->  subPath(i, osm.columnLastIndex) }.filter { it.nonEmpty() },
                (0..osm.columnLastIndex).map { j -> subPath(osm.rowLastIndex, j) }.filter { it.nonEmpty() }
        ).maxBy { path -> evaluatePath(path, osm) }
    }

    fun resample(existing: BSpline, overlapping: BSpline, path: OverlappingPath): Array<ParamPoint> {
        require(path.nonEmpty()) { "empty overlapping path" }

        val (beginI, beginJ) = path.path.head()
        val (endI, endJ) = path.path.last()
        val te = existing.sample(samplingSpan)
        val to = overlapping.sample(samplingSpan)

        fun OverlappingPath.blendData(te: Array<ParamPoint>, to: Array<ParamPoint>): Array<ParamPoint> {
            return this.path.map { (i, j) -> te[i].divide(blendingRate, to[j]) }
        }
        return when(path.overlappingCase()){
            OverlappingCase.ExistingOverlapping -> rearrangeParam(te.take(beginI), path.blendData(te, to), to.drop(endJ))
            OverlappingCase.OverlappingExisting -> rearrangeParam(to.take(beginJ), path.blendData(te, to), te.drop(endI))
            OverlappingCase.ExistingOverlappingExisting -> rearrangeParam(te.take(beginI), path.blendData(te, to), te.drop(endI))
            OverlappingCase.OverlappingExistingOverlapping -> rearrangeParam(to.take(beginJ), path.blendData(te, to), to.drop(endJ))
        }
    }

    fun rearrangeParam(front: Array<ParamPoint>, middle: Array<ParamPoint>, back: Array<ParamPoint>): Array<ParamPoint> {
        val f = front
        val m = middle.let {
            val range = Interval(f.last().param, f.last().param + it.last().param - it.head().param)
            transformParams(it, range)
        }
        val b = back.let {
            val range = Interval(m.last().param, m.last().param + it.last().param - it.head().param)
            transformParams(it, range)
        }
        return Stream.concat(f, m, b).toArray()
    }
}
