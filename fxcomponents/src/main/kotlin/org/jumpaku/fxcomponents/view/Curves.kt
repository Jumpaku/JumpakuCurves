package org.jumpaku.fxcomponents.view

import io.vavr.collection.Array
import javafx.scene.Parent
import javafx.scene.paint.Color
import javafx.scene.shape.*
import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.FuzzyCurve
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.polyline.Polyline
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2
import tornadofx.*


fun Parent.fuzzyPoint(point: Point, op: (Circle.() -> Unit)) {
    group {
        circle(point.x, point.y, point.r) {
            fill = Color.gray(0.0, 0.0)
            op()
        }
    }
}

fun Parent.fuzzyPoints(points: Array<Point>, op: (Circle.() -> Unit)) {
    group {
        points.forEach {
            circle(it.x, it.y, it.r){
                fill = Color.gray(0.0, 0.0)
                op()
            }
        }
    }
}

fun Parent.quadFsc(bSpline: BSpline, deltaT: Double = 0.1, op: (Shape.() -> Unit)) {
    group {
        val x: Shape.() -> Unit = {
            fill = Color.gray(0.0, 0.0)
            op()
        }
        bSpline.toBeziers().forEach {
            val cp = it.controlPoints
            quadcurve(
                    startX = cp[0].x, startY = cp[0].y,
                    controlX = cp[1].x, controlY = cp[1].y,
                    endX = cp[2].x, endY = cp[2].y,
                    op = x)
        }
        bSpline.evaluateAll(deltaT).forEach {
            circle(it.x, it.y, it.r, x)
        }
    }
}

fun Parent.cubicFsc(bSpline: BSpline, deltaT: Double = 0.01, op: (Shape.() -> Unit)) {
    group {
        val x: Shape.() -> Unit = {
            fill = Color.gray(0.0, 0.0)
            op()
        }
        bSpline.toBeziers().forEach {
            val cp = it.controlPoints
            cubiccurve(
                    startX = cp[0].x, startY = cp[0].y,
                    controlX1 = cp[1].x, controlY1 = cp[1].y,
                    controlX2 = cp[2].x, controlY2 = cp[2].y,
                    endX = cp[3].x, endY = cp[3].y,
                    op = x)
        }
        bSpline.evaluateAll(deltaT).forEach {
            circle(it.x, it.y, it.r, x)
        }
    }
}

fun Parent.polyline(polyline: Polyline, op: (Shape.() -> Unit)) {
    group {
        val x: Shape.() -> Unit = {
            fill = Color.gray(0.0, 0.0)
            op()
        }
        when{
            polyline.points.size() <= 1 -> polyline.points
                    .forEach { fuzzyPoint(it, x) }
            else -> {
                polyline.points.zip(polyline.points.tail())
                        .forEach { (a, b) -> line(a.x, a.y, b.x, b.y, x)}
                polyline.points
                        .forEach { circle(it.x, it.y, it.r, x) }
            }
        }
    }
}

fun Parent.fuzzyCurve(fuzzyCurve: FuzzyCurve, delta: Double = 5.0, op: (Shape.() -> Unit)) {
    polyline(Polyline(fuzzyCurve.toArcLengthCurve().evaluateAll(delta)), op)
}