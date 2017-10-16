package jumpaku.fxcomponents.view

import io.vavr.collection.Stream
import javafx.scene.Parent
import javafx.scene.shape.Line
import jumpaku.fsc.snap.Grid
import tornadofx.line


fun Parent.grid(grid: Grid, width: Double, height: Double, op: Line.()->Unit) {
    Stream.from(0).map { grid.origin.x + it * grid.gridSpacing }.takeWhile { it <= width }
            .appendAll(Stream.from(-1).map { grid.origin.x - it * grid.gridSpacing }.takeWhile { it >= 0 })
            .forEach { line(it, 0, it, height, op) }
    Stream.from(0).map { grid.origin.y + it * grid.gridSpacing }.takeWhile { it <= height }
            .appendAll(Stream.from(-1).map { grid.origin.y - it * grid.gridSpacing }.takeWhile { it >= 0 })
            .forEach { line(0, it, width, it, op) }
}