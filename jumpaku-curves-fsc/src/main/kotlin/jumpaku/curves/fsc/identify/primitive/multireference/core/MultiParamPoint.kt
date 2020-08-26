package jumpaku.curves.fsc.identify.primitive.multireference.core

import jumpaku.curves.core.geom.Lerpable
import jumpaku.curves.core.geom.lerp

data class MultiParamPoint(val point: MultiPoint, val param: Double) : Lerpable<MultiParamPoint> {

    init {
        require(param.isFinite()) { "param($param)" }
    }

    override fun lerp(terms: List<Pair<Double, MultiParamPoint>>): MultiParamPoint = MultiParamPoint(
            point.lerp(terms.map { (c, p) -> c to p.point }),
            param.lerp(terms.map { (c, p) -> c to p.param }))

    override fun lerp(t: Double, p: MultiParamPoint): MultiParamPoint {
        return MultiParamPoint(point.lerp(t, p.point), param.lerp(t, p.param))
    }
}