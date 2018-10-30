package jumpaku.fsc.blend

import jumpaku.core.curve.Interval
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.transformParams
import jumpaku.core.util.Option
import jumpaku.core.util.orDefault
import jumpaku.core.util.toOption


typealias PathEvaluator =
        OverlapPath.(osm: OverlapMatrix, existSamples: List<ParamPoint>, overlapSamples: List<ParamPoint>) -> Double
class Blender(
        val samplingSpan: Double = 1.0/128,
        val blendingRate: Double = 0.5,
        val evaluatePath: PathEvaluator = { _, _, _ -> grade.value }) {

    fun blend(existing: BSpline, overlapping: BSpline): Option<List<ParamPoint>> {
        val existSamples = existing.sample(samplingSpan)
        val overlapSamples = overlapping.sample(samplingSpan)
        val overlapState = OverlapState.create(existSamples.map { it.point }, overlapSamples.map { it.point })
        val path = overlapState.paths.maxBy {
            it.evaluatePath(overlapState.osm, existSamples, overlapSamples)
        }.toOption()
        return path.map { resample(existing, overlapping, it) }
    }

    fun resample(existing: BSpline, overlapping: BSpline, path: OverlapPath): List<ParamPoint> {
        val (beginI, beginJ) = path.first()
        val (endI, endJ) = path.last()
        val te = existing.sample(samplingSpan)
        val to = overlapping.sample(samplingSpan)

        fun OverlapPath.blendData(te: List<ParamPoint>, to: List<ParamPoint>): List<ParamPoint> =
                map { (i, j) -> te[i].divide(blendingRate, to[j]) }

        return when(path.type){
            OverlapType.ExistOverlap ->
                rearrangeParam(te.take(beginI), path.blendData(te, to), to.drop(endJ))
            OverlapType.OverlapExist ->
                rearrangeParam(to.take(beginJ), path.blendData(te, to), te.drop(endI))
            OverlapType.ExistOverlapExist ->
                rearrangeParam(te.take(beginI), path.blendData(te, to), te.drop(endI))
            OverlapType.OverlapExistOverlap ->
                rearrangeParam(to.take(beginJ), path.blendData(te, to), to.drop(endJ))
        }
    }

    fun rearrangeParam(front: List<ParamPoint>, middle: List<ParamPoint>, back: List<ParamPoint>): List<ParamPoint> {
        val f = front

        val m = if (front.isEmpty()) middle
        else transformParams(middle, Interval(f.last().param, f.last().param + middle.last().param - middle.first().param))
                .value().orDefault { middle.map { it.copy(param = f.last().param) } }

        val b = if (back.isEmpty()) back
        else transformParams(back, Interval(m.last().param, m.last().param + back.last().param - back.first().param))
                .value().orDefault { back.map { it.copy(param = m.last().param) } }

        return f + m + b
    }
}
