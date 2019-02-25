package jumpaku.fxcomponents.nodes

import javafx.scene.Parent
import javafx.scene.paint.Color
import javafx.scene.shape.*
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.util.asVavr
import tornadofx.*

fun Parent.fuzzyPoints(points: List<Point>, op: (Circle.() -> Unit)): Unit =
        points.forEach { circle(it.x, it.y, it.r) { fill = Color.gray(0.0, 0.0); op() } }

fun Parent.cubicFsc(bSpline: BSpline, deltaT: Double = 0.01, op: (Shape.() -> Unit)): Unit {
    cubicSpline(bSpline, op)
    fuzzyPoints(bSpline.evaluateAll(deltaT), op)
}

fun Parent.cubicSpline(bSpline: BSpline, op: (Shape.() -> Unit)): Unit =
        bSpline.toBeziers().forEach {
            val cp = it.controlPoints
            cubiccurve(
                    startX = cp[0].x, startY = cp[0].y,
                    controlX1 = cp[1].x, controlY1 = cp[1].y,
                    controlX2 = cp[2].x, controlY2 = cp[2].y,
                    endX = cp[3].x, endY = cp[3].y) {
                fill = Color.gray(0.0, 0.0); op()
            }
        }


fun Parent.polyline(polyline: Polyline, op: (Shape.() -> Unit)): Unit = when {
    polyline.points.size <= 1 -> polyline.points.forEach { circle(it.x, it.y, it.r) { fill = Color.gray(0.0, 0.0); op() } }
    else -> {
        path {
            val (x, y) = polyline.points.first()
            moveTo(x, y)
            polyline.points.asVavr().tail().forEach { lineTo(it.x, it.y) }
            op()
        }
        Unit
    }
}

fun Parent.curve(curve: Curve, delta: Double = 5.0, op: (Shape.() -> Unit)) {
    val c = ReparametrizedCurve.approximate(curve, 1.0)
    polyline(Polyline.byArcLength(c.evaluateAll(delta/c.chordLength)), op)
}

fun Parent.fuzzyCurve(curve: Curve, delta: Double = 5.0, op: (Shape.() -> Unit)) {
    val c = ReparametrizedCurve.approximate(curve, 1.0)
    val n = c.chordLength/delta + 1
    fuzzyPoints(curve.evaluateAll(curve.domain.span/n), op)
}
