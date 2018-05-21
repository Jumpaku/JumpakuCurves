package jumpaku.fsc.blend

import io.vavr.collection.Array
import io.vavr.collection.Stream
import io.vavr.control.Option
import jumpaku.core.geom.ParamPoint
import jumpaku.core.curve.Interval
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.geom.transformParams
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.component1
import jumpaku.core.util.component2


class Blender(
        val samplingSpan: Double = 1.0/128,
        val blendingRate: Double = 0.5,
        val evaluatePath: OverlappingMatrix.(OverlappingPath) -> Grade = { it.grade }) {

    fun blend(existing: BSpline, overlapping: BSpline): BlendResult {
        val osm = overlappingMatrix(samplingSpan, existing, overlapping)
        val path = osm.searchPath(evaluatePath)
        return path.map {
            val data = resample(existing, overlapping, it)
            BlendResult(osm, path, path.map { data })
        }.getOrElse { BlendResult(osm, Option.none(), Option.none()) }
    }

    fun overlappingMatrix(samplingSpan: Double, existing: BSpline, overlapping: BSpline): OverlappingMatrix {
        val existingTimes = existing.domain.sample(samplingSpan)
        val overlappingTimes = overlapping.domain.sample(samplingSpan)
        return OverlappingMatrix(existingTimes.map { et ->
            overlappingTimes.map { ot ->
                existing(et).isPossible(overlapping(ot))
            }
        })
    }

    fun resample(existing: BSpline, overlapping: BSpline, path: OverlappingPath): Array<ParamPoint> {
        val (beginI, beginJ) = path.path.head()
        val (endI, endJ) = path.path.last()
        val te = existing.sample(samplingSpan)
        val to = overlapping.sample(samplingSpan)

        fun OverlappingPath.blendData(te: Array<ParamPoint>, to: Array<ParamPoint>): Array<ParamPoint> =
                this.path.map { (i, j) -> te[i].divide(blendingRate, to[j]) }

        return when(path.type){
            OverlappingType.ExistingOverlapping -> rearrangeParam(te.take(beginI), path.blendData(te, to), to.drop(endJ))
            OverlappingType.OverlappingExisting -> rearrangeParam(to.take(beginJ), path.blendData(te, to), te.drop(endI))
            OverlappingType.ExistingOverlappingExisting -> rearrangeParam(te.take(beginI), path.blendData(te, to), te.drop(endI))
            OverlappingType.OverlappingExistingOverlapping -> rearrangeParam(to.take(beginJ), path.blendData(te, to), to.drop(endJ))
        }
    }

    fun rearrangeParam(front: Array<ParamPoint>, middle: Array<ParamPoint>, back: Array<ParamPoint>): Array<ParamPoint> {
        val f = front
        val m = if (front.isEmpty) middle
        else transformParams(
                middle, Interval(f.last().param, f.last().param + middle.last().param - middle.head().param))
                .getOrElse { middle.map { it.copy(param = f.last().param) } }
        val b = if (back.isEmpty) back
        else transformParams(
                back, Interval(m.last().param, m.last().param + back.last().param - back.head().param))
                .getOrElse { back.map { it.copy(param = m.last().param) } }

        return Stream.concat(f, m, b).toArray()
    }
}
