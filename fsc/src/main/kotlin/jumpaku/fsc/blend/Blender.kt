package jumpaku.fsc.blend

import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.Interval
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.transformParams
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.none
import jumpaku.core.util.orDefault


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
        }.orDefault { BlendResult(osm, none(), none()) }
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

    fun resample(existing: BSpline, overlapping: BSpline, path: OverlappingPath): List<ParamPoint> {
        val (beginI, beginJ) = path.path.first()
        val (endI, endJ) = path.path.last()
        val te = existing.sample(samplingSpan)
        val to = overlapping.sample(samplingSpan)

        fun OverlappingPath.blendData(te: List<ParamPoint>, to: List<ParamPoint>): List<ParamPoint> =
                this.path.map { (i, j) -> te[i].divide(blendingRate, to[j]) }

        return when(path.type){
            OverlappingType.ExistingOverlapping -> rearrangeParam(te.take(beginI), path.blendData(te, to), to.drop(endJ))
            OverlappingType.OverlappingExisting -> rearrangeParam(to.take(beginJ), path.blendData(te, to), te.drop(endI))
            OverlappingType.ExistingOverlappingExisting -> rearrangeParam(te.take(beginI), path.blendData(te, to), te.drop(endI))
            OverlappingType.OverlappingExistingOverlapping -> rearrangeParam(to.take(beginJ), path.blendData(te, to), to.drop(endJ))
        }
    }

    fun rearrangeParam(front: List<ParamPoint>, middle: List<ParamPoint>, back: List<ParamPoint>): List<ParamPoint> {
        val f = front
        val m = if (front.isEmpty()) middle
        else transformParams(
                middle, Interval(f.last().param, f.last().param + middle.last().param - middle.first().param))
                .orDefault { middle.map { it.copy(param = f.last().param) } }
        val b = if (back.isEmpty()) back
        else transformParams(
                back, Interval(m.last().param, m.last().param + back.last().param - back.first().param))
                .orDefault { back.map { it.copy(param = m.last().param) } }

        return f + m + b
    }
}
