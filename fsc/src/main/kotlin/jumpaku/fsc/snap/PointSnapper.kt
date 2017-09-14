package jumpaku.fsc.snap

import jumpaku.core.affine.Point

data class PointSnapResult(
        val snapped: Point)

class PointSnapper(
        val grid: BaseGrid,
        val minResolution: Int = 0,
        val maxResolution: Int = 0) {
    fun snap(point: Point): PointSnapResult = TODO()
}