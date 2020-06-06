package jumpaku.curves.fsc.snap.point

import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.GridPoint


fun Grid.transformToWorld(pointSnapResult: PointSnapResult): Point =
        pointSnapResult.run { transformToWorld(gridPoint, resolution) }

class PointSnapResult(
        val resolution: Int,
        val gridPoint: GridPoint,
        val grade: Grade)

