package jumpaku.curves.graphics

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Sampler
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.polyline.LineSegment
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.transform.Calibrate
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.conicsection.ConjugateBox
import org.apache.commons.math3.util.FastMath
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.*
import kotlin.math.PI
import kotlin.math.sqrt


fun Graphics2D.clearRect(x: Double, y: Double, width: Double, height: Double) =
        clearRect(x.toInt(), y.toInt(), width.toInt(), height.toInt())

fun Graphics2D.clearRect(rectangle2D: Rectangle2D) =
        clearRect(rectangle2D.x, rectangle2D.y, rectangle2D.width, rectangle2D.height)


fun Graphics2D.drawShape(shape: Shape, style: (Graphics2D) -> Unit = DrawStyle()) {
    style(this)
    draw(shape)
}

fun Graphics2D.fillShape(shape: Shape, style: (Graphics2D) -> Unit = FillStyle()) {
    style(this)
    fill(shape)
}


private fun makePointShape(point: Point): Shape =
        Ellipse2D.Double(point.x - point.r, point.y - point.r, point.r * 2, point.r * 2)

fun Graphics2D.drawPoint(point: Point, style: (Graphics2D) -> Unit = DrawStyle()) =
        drawShape(makePointShape(point), style)

fun Graphics2D.fillPoint(point: Point, style: (Graphics2D) -> Unit = FillStyle()) =
        fillShape(makePointShape(point), style)

fun Graphics2D.drawPoints(points: List<Point>, style: (Graphics2D) -> Unit = DrawStyle()) =
        points.forEach { drawPoint(it, style) }

fun Graphics2D.fillPoints(points: List<Point>, style: (Graphics2D) -> Unit = FillStyle()) =
        points.forEach { fillPoint(it, style) }


private fun makeLineShape(line: LineSegment): Shape = line.run {
    Path2D.Double().apply {
        moveTo(begin.x, begin.y)
        lineTo(end.x, end.y)
    }
}
fun Graphics2D.drawLineSegment(line: LineSegment, style: (Graphics2D) -> Unit = DrawStyle()) =
        drawShape(makeLineShape(line), style)

fun Graphics2D.drawPolyline(polyline: Polyline, style: (Graphics2D) -> Unit = DrawStyle()) =
        polyline.paramPoints.zipWithNext().forEach { (p0, p1) -> drawLineSegment(LineSegment(p0, p1), style) }

fun Graphics2D.drawGrid(
        grid: Grid,
        resolution: Int,
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        style: (Graphics2D) -> Unit = DrawStyle()) {
    val o = Point(grid.baseGridToWorld.move())
    val s = grid.spacingInWorld(resolution)
    val t = grid.baseGridToWorld.rotation().at(o)
    val vs = (FastMath.ceil((x - o.x) / s).toInt()..FastMath.floor((x - o.x + w) / s).toInt())
            .map { o.x + s * it }
            .map { LineSegment(t(Point.xy(it, y)), t(Point.xy(it, y + h))) }
    val hs = (FastMath.ceil((y - o.y) / s).toInt()..FastMath.floor((y - o.y + h) / s).toInt())
            .map { o.y + s * it }
            .map { LineSegment(t(Point.xy(x, it)), t(Point.xy(x + w, it))) }
    (vs + hs).forEach { drawLineSegment(it, style) }
}

fun Graphics2D.drawGrid(
        grid: Grid,
        resolution: Int,
        rectangle2D: Rectangle2D,
        style: (Graphics2D) -> Unit = DrawStyle()) {
    val g = this
    rectangle2D.run { g.drawGrid(grid, resolution, x, y, width, height, style) }
}

fun Graphics2D.drawConjugateBox(conjugateBox: ConjugateBox, style: (Graphics2D) -> Unit = DrawStyle()) {
    conjugateBox.run { drawPolyline(Polyline.byIndices(bottomLeft, topLeft, topRight, bottomRight, bottomLeft), style) }
    conjugateBox.run { drawPolyline(Polyline.byIndices(left, top, right, bottom, left), style) }
}


private fun makeCubicBezier(bezier: Bezier): Shape {
    val (p0, p1, p2, p3) = bezier.controlPoints
    return CubicCurve2D.Double(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
}

fun Graphics2D.drawCubicBezier(bezier: Bezier, style: (Graphics2D) -> Unit = DrawStyle()) {
    require(bezier.degree <= 3) { "drawCubicBSpline degree(${bezier.degree})" }
    var b = bezier
    for (i in bezier.degree until 3) b = b.elevate()
    drawShape(makeCubicBezier(b), style)
}

fun Graphics2D.drawCubicBSpline(bSpline: BSpline, style: (Graphics2D) -> Unit = DrawStyle()) {
    require(bSpline.degree >= 3) { "drawCubicBSpline degree(${bSpline.degree})" }
    bSpline.toBeziers().map { (1..(it.degree - 3)).fold(it) { r, _ -> r.reduce() } }.forEach { drawCubicBezier(it, style) }
}


private fun makeConicSection(conicSection: ConicSection): Shape {
    val b0 = conicSection.begin
    val f = conicSection.far
    val b2 = conicSection.end
    if (conicSection.weight >= 1.0) return Line2D.Double(b0.x, b0.y, b2.x, b2.y)
    val w = conicSection.weight
    val extent = FastMath.acos(w) * 2
    val start = (-PI - extent) * 0.5
    val arc = Arc2D.Double(-1.0, -1.0, 2.0, 2.0, start * 180 / PI, extent * 180 / PI, Arc2D.OPEN)
    val transform = Calibrate(
            Point.xy(-sqrt(1 - w * w), w) to b0,
            Point.xy(0.0, 1.0) to f,
            Point.xy(sqrt(1 - w * w), w) to b2).matrix.run {
        AffineTransform(
                getEntry(0, 0),
                getEntry(1, 0),
                getEntry(0, 1),
                getEntry(1, 1),
                getEntry(0, 3),
                getEntry(1, 3))
    }
    return transform.createTransformedShape(arc)
}

fun Graphics2D.drawConicSection(conicSection: ConicSection, style: (Graphics2D) -> Unit = DrawStyle()) =
        drawShape(makeConicSection(conicSection), style)


fun Graphics2D.drawCurve(curve: Curve, nSamples: Int, style: (Graphics2D) -> Unit = DrawStyle()) = when {
    curve is Bezier && curve.degree == 3 -> drawCubicBezier(curve, style)
    curve is BSpline && curve.degree == 3 -> drawCubicBSpline(curve, style)
    curve is ConicSection -> drawConicSection(curve, style)
    curve is Polyline -> drawPolyline(curve, style)
    curve is LineSegment -> drawLineSegment(curve, style)
    curve is DrawingStroke -> drawPolyline(Polyline(curve.inputData), style)
    else -> drawPolyline(Polyline(curve.sample(Sampler(nSamples))), style)
}
