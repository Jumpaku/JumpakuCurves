package jumpaku.fxcomponents.nodes

import javafx.scene.Parent
import javafx.scene.paint.Color
import javafx.scene.shape.Shape
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.GridPoint
import jumpaku.curves.fsc.snap.conicsection.ConjugateBox
import jumpaku.curves.fsc.snap.point.PointSnapResult
import jumpaku.curves.fsc.snap.toWorldPoint
import org.apache.commons.math3.util.FastMath
import tornadofx.circle
import tornadofx.line

fun Parent.grid(grid: Grid, x: Double, y: Double, w: Double, h: Double, op: Shape.()->Unit) {
    val o = grid.origin
    val s = grid.baseSpacing
    val t = grid.rotation.at(o)
    (FastMath.ceil((x - o.x)/s).toInt()..FastMath.floor((x - o.x + w)/s).toInt())
            .map { o.x + s * it }
            .forEach {
                val b = t(Point.xy(it, y))
                val e = t(Point.xy(it, y + h))
                line(b.x, b.y, e.x, e.y, op)
            }
    (FastMath.ceil((y - o.y)/s).toInt()..FastMath.floor((y - o.y + h)/s).toInt())
            .map { o.y + s * it }
            .forEach {
                val b = t(Point.xy(x, it))
                val e = t(Point.xy(x + w, it))
                line(b.x, b.y, e.x, e.y, op)
            }
}

fun Parent.snappedPoint(grid: Grid, pointSnapResult: PointSnapResult, op: Shape.()->Unit) {
    val gp = pointSnapResult.gridPoint
    val r = pointSnapResult.resolution
    val g00 = grid.toWorldPoint(GridPoint(gp.x - 1, gp.y - 1, gp.z), r)
    val g01 = grid.toWorldPoint(GridPoint(gp.x + 0, gp.y - 1, gp.z), r)
    val g02 = grid.toWorldPoint(GridPoint(gp.x + 1, gp.y - 1, gp.z), r)
    val g10 = grid.toWorldPoint(GridPoint(gp.x - 1, gp.y + 0, gp.z), r)
    val g11 = grid.toWorldPoint(GridPoint(gp.x + 0, gp.y + 0, gp.z), r)
    val g12 = grid.toWorldPoint(GridPoint(gp.x + 1, gp.y + 0, gp.z), r)
    val g20 = grid.toWorldPoint(GridPoint(gp.x - 1, gp.y + 1, gp.z), r)
    val g21 = grid.toWorldPoint(GridPoint(gp.x + 0, gp.y + 1, gp.z), r)
    val g22 = grid.toWorldPoint(GridPoint(gp.x + 1, gp.y + 1, gp.z), r)
    line(g00.x, g00.y, g02.x, g02.y, op)
    line(g10.x, g10.y, g12.x, g12.y, op)
    line(g20.x, g20.y, g22.x, g22.y, op)
    line(g00.x, g00.y, g20.x, g20.y, op)
    line(g01.x, g01.y, g21.x, g21.y, op)
    line(g02.x, g02.y, g22.x, g22.y, op)
    circle(g11.x, g11.y, 3.0) { fill = Color.gray(0.0 ,0.0); op() }
}

fun Parent.conjugateBox(conjugateBox: ConjugateBox, op: Shape.()->Unit): Unit {
    polyline(Polyline.byArcLength(conjugateBox.bottomLeft, conjugateBox.topLeft, conjugateBox.topRight, conjugateBox.bottomRight, conjugateBox.bottomLeft), op)
    polyline(Polyline.byArcLength(conjugateBox.left, conjugateBox.top, conjugateBox.right, conjugateBox.bottom, conjugateBox.left), op)
}
