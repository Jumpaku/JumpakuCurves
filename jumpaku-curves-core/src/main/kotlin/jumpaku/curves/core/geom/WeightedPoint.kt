package jumpaku.curves.core.geom



fun Point.weighted(weight: Double = 1.0): WeightedPoint = WeightedPoint(this, weight)

data class WeightedPoint(val point: Point, val weight: Double = 1.0) : Lerpable<WeightedPoint> {

    override fun lerp(vararg terms: Pair<Double, WeightedPoint>): WeightedPoint {
        val w = weight.lerp(*terms.map { (c, wp) -> c to wp.weight }.toTypedArray())
        val p = point.lerp(*terms.map { (c, wp) -> (c * wp.weight / w) to wp.point }.toTypedArray())
        return WeightedPoint(p, w)
    }


}


