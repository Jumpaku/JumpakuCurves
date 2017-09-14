package jumpaku.fsc.blend

import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.HashMap
import io.vavr.collection.Stream
import io.vavr.control.Option
import jumpaku.core.curve.Interval
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fit.transformParams
import jumpaku.core.util.*
import jumpaku.fsc.generate.FscGenerator


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
        require(middle.nonEmpty()) { "non overlapping"}

        val f = front
        val m = when {
            front.isEmpty -> middle
            else -> transformParams(middle,
                    Interval(f.last().param, f.last().param + middle.last().param - middle.head().param))
        }
        val b = when {
            back.isEmpty -> back
            else -> transformParams(back,
                    Interval(m.last().param, m.last().param + back.last().param - back.head().param))
        }

        return Stream.concat(f, m, b).toArray()
    }
}
