package jumpaku.fxcomponents.nodes

import javafx.scene.Parent
import javafx.scene.paint.Color
import javafx.scene.shape.Shape
import jumpaku.core.affine.Point
import jumpaku.core.affine.rotation
import jumpaku.core.affine.transformationAt
import jumpaku.core.curve.polyline.Polyline
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.GridPoint
import jumpaku.fsc.snap.conicsection.ConjugateBox
import jumpaku.fsc.snap.point.PointSnapResult
import org.apache.commons.math3.util.FastMath
import tornadofx.circle
import tornadofx.line

fun Parent.grid(grid: Grid, x: Double, y: Double, w: Double, h: Double, op: Shape.()->Unit): Unit {
    val o = grid.origin
    val s = grid.spacing
    val t = transformationAt(o, rotation(grid.axis, grid.radian))
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

fun Parent.snappedPoint(pointSnapResult: PointSnapResult, op: Shape.()->Unit): Unit {
    val a = pointSnapResult.grid.localToWorld
    val g00 = GridPoint(pointSnapResult.gridPoint.x - 1, pointSnapResult.gridPoint.y - 1, pointSnapResult.gridPoint.z).toWorldPoint(a)
    val g01 = GridPoint(pointSnapResult.gridPoint.x + 0, pointSnapResult.gridPoint.y - 1, pointSnapResult.gridPoint.z).toWorldPoint(a)
    val g02 = GridPoint(pointSnapResult.gridPoint.x + 1, pointSnapResult.gridPoint.y - 1, pointSnapResult.gridPoint.z).toWorldPoint(a)
    val g10 = GridPoint(pointSnapResult.gridPoint.x - 1, pointSnapResult.gridPoint.y + 0, pointSnapResult.gridPoint.z).toWorldPoint(a)
    val g11 = GridPoint(pointSnapResult.gridPoint.x + 0, pointSnapResult.gridPoint.y + 0, pointSnapResult.gridPoint.z).toWorldPoint(a)
    val g12 = GridPoint(pointSnapResult.gridPoint.x + 1, pointSnapResult.gridPoint.y + 0, pointSnapResult.gridPoint.z).toWorldPoint(a)
    val g20 = GridPoint(pointSnapResult.gridPoint.x - 1, pointSnapResult.gridPoint.y + 1, pointSnapResult.gridPoint.z).toWorldPoint(a)
    val g21 = GridPoint(pointSnapResult.gridPoint.x + 0, pointSnapResult.gridPoint.y + 1, pointSnapResult.gridPoint.z).toWorldPoint(a)
    val g22 = GridPoint(pointSnapResult.gridPoint.x + 1, pointSnapResult.gridPoint.y + 1, pointSnapResult.gridPoint.z).toWorldPoint(a)
    line(g00.x, g00.y, g02.x, g02.y, op)
    line(g10.x, g10.y, g12.x, g12.y, op)
    line(g20.x, g20.y, g22.x, g22.y, op)
    line(g00.x, g00.y, g20.x, g20.y, op)
    line(g01.x, g01.y, g21.x, g21.y, op)
    line(g02.x, g02.y, g22.x, g22.y, op)
    circle(g11.x, g11.y, 3.0) { fill = Color.gray(0.0 ,0.0); op() }
}

fun Parent.conjugateBox(conjugateBox: ConjugateBox, op: Shape.()->Unit): Unit {
    polyline(Polyline(conjugateBox.bottomLeft, conjugateBox.topLeft, conjugateBox.topRight, conjugateBox.bottomRight, conjugateBox.bottomLeft), op)
    polyline(Polyline(conjugateBox.left, conjugateBox.top, conjugateBox.right, conjugateBox.bottom, conjugateBox.left), op)
}
