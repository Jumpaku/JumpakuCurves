package jumpaku.fsc.snap

import io.vavr.collection.Array
import io.vavr.collection.Stream
import io.vavr.control.Option
import jumpaku.core.affine.Point
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.component1
import jumpaku.core.util.component2

data class PointSnapResult(
        val grid: Option<Grid>,
        val snappedGridCoordinate: Option<GridCoordinate>,
        val snappedPoint: Point)

class PointSnapper(
        val baseGrid: BaseGrid,
        val minResolution: Int = 0,
        val maxResolution: Int = 0) {
    init {
        require(minResolution <= maxResolution) { "minResolution($minResolution) > maxResolution($maxResolution)" }
    }

    fun snap(cursor: Point): PointSnapResult {
        val candidates = Stream.rangeClosed(minResolution, maxResolution)
                .map { baseGrid.deriveGrid(it).snap(cursor) }
                .toArray()
        val ns = candidates.map { Array.of(it.toFuzzyPoint().isNecessary(cursor)) }
                .scan(Array.empty<Grade>(), { acc, n -> acc.appendAll(n) })
                .tail()
                .map { ns -> ns.init().map { !it }.fold(Grade.TRUE, Grade::and) and ns.last() }
        val mus = ns.zip(candidates)
        val snapped = mus
                .filter { (mu, _) -> mu >= Grade(0.5) }
                .toOption()
                .map { (_, c) -> c }

        return PointSnapResult(
                snapped.map { it.grid },
                snapped,
                snapped.map { it.toCrispPoint() }.getOrElse { cursor }.toCrisp())
    }
}