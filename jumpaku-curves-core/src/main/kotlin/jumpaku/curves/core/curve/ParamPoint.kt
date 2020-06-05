package jumpaku.curves.core.curve

import jumpaku.curves.core.geom.Lerpable
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.lerp

data class ParamPoint(val point: Point, val param: Double) : Lerpable<ParamPoint> {

    init {
        require(param.isFinite()) { "param($param)" }
    }

    override fun lerp(terms: List<Pair<Double, ParamPoint>>): ParamPoint = ParamPoint(
            point.lerp(terms.map { (c, p) -> c to p.point }),
            param.lerp(terms.map { (c, p) -> c to p.param }))


}

