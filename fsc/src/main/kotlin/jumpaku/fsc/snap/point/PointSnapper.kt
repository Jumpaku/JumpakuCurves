package jumpaku.fsc.snap.point

import com.google.gson.JsonElement
import io.vavr.collection.Stream
import jumpaku.core.affine.Point
import jumpaku.core.fuzzy.Grade
import jumpaku.core.json.ToJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.snap.BaseGrid
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.GridPoint
import jumpaku.fsc.snap.NoGrid


data class PointSnapResult(
        val grid: Grid,
        val gridPoint: GridPoint,
        val worldPoint: Point,
        val grade: Grade)

fun noPointSnap(baseGrid: BaseGrid, cursor: Point): PointSnapResult
        = PointSnapResult(NoGrid(baseGrid), GridPoint(0, 0, 0), cursor.toCrisp(), Grade.TRUE)

class PointSnapper(
        val baseGrid: BaseGrid,
        val minResolution: Int = 0,
        val maxResolution: Int = 0) {
    init {
        require(minResolution <= maxResolution) { "minResolution($minResolution) > maxResolution($maxResolution)" }
    }

    fun snap(cursor: Point): PointSnapResult {

        val candidates = Stream.rangeClosed(minResolution, maxResolution).map {
            val grid = baseGrid.deriveGrid(it)
            val gridPoint = grid.snapToNearestGrid(cursor)
            val worldPoint = grid
                    .localToWorld(Point.xyz(gridPoint.x.toDouble(), gridPoint.y.toDouble(), gridPoint.z.toDouble()))
            PointSnapResult(grid, gridPoint, worldPoint, worldPoint.copy(r = grid.fuzziness).isNecessary(cursor))
        }.toArray()
        val mus = candidates
                .scanLeft(Stream.empty<PointSnapResult>()) { acc, n -> acc.append(n) }.tail()
                .map { ns -> ns.init().map { !it.grade }.fold(ns.last().grade, Grade::and) }
        return mus.zip(candidates)
                .find { (mu, _) -> mu >= Grade(0.5) }
                .map { (mu, result) -> result.copy(grade = mu) }
                .getOrElse { noPointSnap(baseGrid, cursor.toCrisp()) }
    }
}