package jumpaku.fsc.blend

import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.HashMap
import io.vavr.collection.Stream
import io.vavr.control.Option
import jumpaku.core.affine.divide
import jumpaku.core.curve.Interval
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fit.transformParams
import jumpaku.core.util.*
import jumpaku.fsc.generate.FscGenerator


data class BlendResult(
        val osm: OverlappingMatrix,
        val path: Option<OverlappingPath>,
        val blended: Option<BSpline>)

class Blender(
        val samplingSpan: Double = 1.0/128,
        val blendingRate: Double = 0.5,
        val evaluatePath: OverlappingPath.(OverlappingMatrix) -> Double = { _ -> grade.value }) {

    fun blend(existing: BSpline, overlapping: BSpline): BlendResult {
        val osm = OverlappingMatrix(samplingSpan, existing, overlapping)
        val path = searchPath(osm)
        return BlendResult(osm, path, path.map {
            val data = resample(osm, it)
            data.forEach { println("%3f".format(it.param)) }
            FscGenerator().generate(data)
        })
    }

    fun searchPath(osm: OverlappingMatrix): Option<OverlappingPath> {

        var dpTable = HashMap.empty<Tuple2<Int, Int>, OverlappingPath>()
        fun subPath(i: Int, j: Int): OverlappingPath {
            val path =  dpTable[Tuple2(i, j)].getOrElse {
                val uij = osm[i, j]
                when {
                    i == 0 || j == 0 -> initialPath(uij, i, j)
                    else -> Array.of(subPath(i - 1, j), subPath(i, j - 1), subPath(i - 1, j - 1))
                            .maxBy { (grade, _) -> grade }
                            .map { it.extend(uij, i, j) }
                            .get()
                }
            }
            dpTable = dpTable.put(Tuple2(i, j), path)
            return path
        }
        return Stream.concat(
                osm.existingTimes
                        .zipWithIndex { _, i ->  subPath(i, osm.overlappingTimes.size() - 1) }
                        .filter { it.nonEmpty() },
                osm.overlappingTimes
                        .zipWithIndex { _, j -> subPath(osm.existingTimes.size() - 1, j) }
                        .filter { it.nonEmpty() })
                .maxBy { path -> evaluatePath(path, osm) }
    }

    fun resample(osm: OverlappingMatrix, path: OverlappingPath): Array<ParamPoint> {
        require(path.nonEmpty()) { "empty overlapping path" }

        val (beginI, beginJ) = path.path.head()
        val (endI, endJ) = path.path.last()
        val te = osm.existingTimes
        val to = osm.overlappingTimes
        val se = osm.existing
        val so = osm.overlapping

        fun data(ts: Array<Double>, s: BSpline): Array<ParamPoint> = ts.map { ParamPoint(s(it), it) }
        fun OverlappingPath.blendData(te: Array<Double>, se: BSpline, to: Array<Double>, so: BSpline): Array<ParamPoint> {
            when(osm.overlappingCase(path)) {

            }
            return this.path.map { (i, j) ->
                ParamPoint(se(te[i]).divide(blendingRate, so(to[j])), te[i].divide(blendingRate, to[j]))
            }
        }
        return when(osm.overlappingCase(path)){
            OverlappingCase.ExistingOverlapping ->
                rearrangeParam(data(te.take(beginI), se), path.blendData(te, se, to, so), data(to.drop(endJ), so))
            OverlappingCase.OverlappingExisting ->
                rearrangeParam(data(to.take(beginJ), so), path.blendData(te, se, to, so), data(te.drop(endI), se))
            OverlappingCase.ExistingOverlappingExisting ->
                rearrangeParam(data(te.take(beginI), se), path.blendData(te, se, to, so), data(te.drop(endI), se))
            OverlappingCase.OverlappingExistingOverlapping ->
                rearrangeParam(data(to.take(beginJ), so), path.blendData(te, se, to, so), data(to.drop(endJ), so))
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
