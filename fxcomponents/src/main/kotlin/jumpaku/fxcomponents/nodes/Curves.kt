package jumpaku.fxcomponents.nodes

import io.vavr.collection.Array
import javafx.scene.Parent
import javafx.scene.paint.Color
import javafx.scene.shape.*
import jumpaku.core.geom.Point
import jumpaku.core.curve.Curve
import jumpaku.core.curve.arclength.ReparametrizedCurve
import jumpaku.core.curve.arclength.repeatBisect
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.polyline.Polyline
import tornadofx.*

fun Parent.fuzzyPoints(points: Array<Point>, op: (Circle.() -> Unit)): Unit =
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
    polyline.points.size() <= 1 -> polyline.points.forEach { circle(it.x, it.y, it.r) { fill = Color.gray(0.0, 0.0); op() } }
    else -> {
        path {
            val (x, y) = polyline.points.head()
            moveTo(x, y)
            polyline.points.tail().forEach { lineTo(it.x, it.y) }
            op()
        }
        Unit
    }
}

fun Parent.fuzzyCurve(curve: Curve, delta: Double = 5.0, op: (Shape.() -> Unit)) {
    val c = ReparametrizedCurve(curve,
            repeatBisect(curve, 1.0).map { it.begin }.append(curve.domain.end).toArray())
    val points = c.evaluateAll(delta/c.chordLength)
    fuzzyPoints(points, op)
    polyline(Polyline(points), op)
}