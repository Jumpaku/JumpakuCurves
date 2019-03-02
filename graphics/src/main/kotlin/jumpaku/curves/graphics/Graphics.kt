package jumpaku.curves.graphics

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.geom.Line
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.transform.Calibrate
import jumpaku.curves.fsc.snap.Grid
import org.apache.commons.math3.util.FastMath
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.*
import kotlin.math.PI
import kotlin.math.sqrt


fun Graphics2D.drawShape(style: (Graphics2D)->Unit = DrawStyle(), makeShape: ()-> Shape) {
    style(this)
    draw(makeShape())
}
fun Graphics2D.fillShape(style: (Graphics2D)->Unit = FillStyle(), makeShape: ()->Shape) {
    style(this)
    fill(makeShape())
}



private fun makePointShape(point: Point): Shape =
        Ellipse2D.Double(point.x - point.r*2, point.y - point.r*2, point.r*2, point.r*2)
fun Graphics2D.drawPoint(point: Point, style: (Graphics2D)->Unit = DrawStyle()) =
        drawShape(style) { makePointShape(point) }
fun Graphics2D.fillPoint(point: Point, style: (Graphics2D)->Unit = FillStyle()) =
        fillShape(style) { makePointShape(point) }

fun Graphics2D.drawPoints(points: List<Point>, style: (Graphics2D)->Unit = DrawStyle()) {
    points.forEach { drawShape(style) { makePointShape(it) } }
}
fun Graphics2D.fillPoints(points: List<Point>, style: (Graphics2D)->Unit = FillStyle()) {
    points.forEach { fillShape(style) { makePointShape(it) } }
}



private fun makePolylineShape(polyline: Polyline): Shape {
    val path = Path2D.Double()
    val h = polyline.points.first()
    path.moveTo(h.x, h.y)
    for (p in polyline.points.drop(1)) {
        path.lineTo(p.x, p.y)
    }
    return path
}
fun Graphics2D.drawPolyline(polyline: Polyline, style: (Graphics2D)->Unit = DrawStyle()) {
    require(polyline.points.isNotEmpty()) { "empty points" }
    drawShape(style) { makePolylineShape(polyline) }
}
fun Graphics2D.fillPolyline(polyline: Polyline, style: (Graphics2D)->Unit = FillStyle()) {
    require(polyline.points.isNotEmpty()) { "empty points" }
    fillShape(style) { makePolylineShape(polyline) }
}



private fun makeCubicBezier(bezier: Bezier): Shape {
    val path = Path2D.Double()
    val (p0, p1, p2, p3) = bezier.controlPoints
    path.moveTo(p0.x, p0.y)
    path.curveTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
    return path
}
fun Graphics2D.drawCubicBezier(bezier: Bezier, style: (Graphics2D)->Unit = DrawStyle()) {
    require(bezier.degree == 3) { "drawCubicBSpline degree(${bezier.degree})"}
    drawShape(style) { makeCubicBezier(bezier) }
}
fun Graphics2D.fillCubicBezier(bezier: Bezier, style: (Graphics2D)->Unit = FillStyle()) {
    require(bezier.degree == 3) { "drawCubicBSpline degree(${bezier.degree})"}
    fillShape(style) { makeCubicBezier(bezier) }
}

fun Graphics2D.drawCubicBSpline(bSpline: BSpline, style: (Graphics2D)->Unit = DrawStyle()) {
    require(bSpline.degree == 3) { "drawCubicBSpline degree(${bSpline.degree})"}
    bSpline.toBeziers().forEach { drawCubicBezier(it, style) }
}
fun Graphics2D.fillCubicBSpline(bSpline: BSpline, style: (Graphics2D)->Unit = FillStyle()) {
    require(bSpline.degree == 3) { "drawCubicBSpline degree(${bSpline.degree})"}
    bSpline.toBeziers().forEach { fillCubicBezier(it, style) }
}



private fun makeConicSection(conicSection: ConicSection): Shape {
    val b0 = conicSection.begin
    val f = conicSection.far
    val b2 = conicSection.end
    if (conicSection.weight >= 1.0) return Line2D.Double(b0.x, b0.y, b2.x, b2.y)
    val w = conicSection.weight
    val extent = FastMath.acos(w)*2
    val start = (PI - extent)*0.5
    val arc = Arc2D.Double(-1.0, -1.0, 2.0, 2.0, start, extent, Arc2D.OPEN)
    val transform = Calibrate(
            Point.xy(sqrt(1 - w*w), w) to b0,
            Point.xy(0.0, 1.0) to f,
            Point.xy(-sqrt(1 - w*w), w) to b2).matrix.run {
        AffineTransform(getEntry(0,0), getEntry(1,0), getEntry(0,1), getEntry(1,1), getEntry(0,3), getEntry(1,3))
    }
    return transform.createTransformedShape(arc)
}
fun Graphics2D.drawConicSection(conicSection: ConicSection, style: (Graphics2D)->Unit = DrawStyle()) {
    drawShape(style) { makeConicSection(conicSection) }
}
fun Graphics2D.fillConicSection(conicSection: ConicSection, style: (Graphics2D)->Unit = FillStyle()) {
    fillShape(style) { makeConicSection(conicSection) }
}




fun Graphics2D.drawConicSection(curve: Curve, nSamples: Int, style: (Graphics2D)->Unit = DrawStyle()) {
    drawShape(style) { makePolylineShape(Polyline(curve.sample(nSamples))) }
}
fun Graphics2D.fillConicSection(curve: Curve, nSamples: Int, style: (Graphics2D)->Unit = FillStyle()) {
    fillShape(style) { makePolylineShape(Polyline(curve.sample(nSamples))) }
}



private fun makeLineShape(line: Line): Shape = line.run { Line2D.Double(p0.x, p0.y, p1.x, p1.y) }
fun Graphics2D.drawGrids(
        grid: Grid,
        x: Double = 0.0,
        y: Double = 0.0,
        w: Double,
        h: Double,
        resolution: Int = 0,
        style: (Graphics2D)->Unit = DrawStyle()) {

    val o = grid.origin
    val s = grid.spacing(resolution)
    val t = grid.rotation.at(o)
    val vs = (FastMath.ceil((x - o.x)/s).toInt()..FastMath.floor((x - o.x + w)/s).toInt())
            .map { o.x + s * it }
            .map { Line(t(Point.xy(it, y)), t(Point.xy(it, y + h))) }
    val hs = (FastMath.ceil((y - o.y)/s).toInt()..FastMath.floor((y - o.y + h)/s).toInt())
            .map { o.y + s * it }
            .map { Line(t(Point.xy(x, it)), t(Point.xy(x + w, it))) }
    (vs + hs).forEach { drawShape(style) { makeLineShape(it) } }
}