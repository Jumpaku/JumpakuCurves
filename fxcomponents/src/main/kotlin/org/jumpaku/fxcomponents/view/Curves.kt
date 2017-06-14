package org.jumpaku.fxcomponents.view

import io.vavr.collection.Array
import javafx.scene.Parent
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.CubicCurve
import javafx.scene.shape.Line
import javafx.scene.shape.Shape
import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.polyline.Polyline
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2
import tornadofx.circle
import tornadofx.cubiccurve
import tornadofx.group
import tornadofx.line


fun Parent.fuzzyPoint(point: Point, op: (Circle.() -> Unit) = { Unit }): Unit {
    group {
        circle(point.x, point.y, point.r) {
            fill = Color.gray(0.0, 0.0)
            op()
        }
    }
}

fun Parent.fuzzyPoints(points: Array<Point>, op: (Circle.() -> Unit) = { Unit }): Unit {
    group {
        points.forEach {
            circle(it.x, it.y, it.r){
                fill = Color.gray(0.0, 0.0)
                op()
            }
        }
    }
}

fun Parent.cubicBSpline(bSpline: BSpline, op: (CubicCurve.() -> Unit) = { Unit }): Unit {
    group {
        bSpline.toBeziers().forEach {
            val cp = it.controlPoints
            cubiccurve(
                    startX = cp[0].x, startY = cp[0].y,
                    controlX1 = cp[1].x, controlY1 = cp[1].y,
                    controlX2 = cp[2].x, controlY2 = cp[2].y,
                    endX = cp[3].x, endY = cp[3].y){
                fill = Color.gray(0.0, 0.0)
                op()
            }
        }
    }
}

fun Parent.cubicFsc(bSpline: BSpline, op: (Shape.() -> Unit) = { Unit }): Unit {
    group {
        bSpline.toBeziers().forEach {
            val cp = it.controlPoints
            cubiccurve(
                    startX = cp[0].x, startY = cp[0].y,
                    controlX1 = cp[1].x, controlY1 = cp[1].y,
                    controlX2 = cp[2].x, controlY2 = cp[2].y,
                    endX = cp[3].x, endY = cp[3].y){
                fill = Color.gray(0.0, 0.0)
                op()
            }
        }
        bSpline.evaluateAll(0.01).forEach {
            circle(it.x, it.y, it.r){
                fill = Color.gray(0.0, 0.0)
                op()
            }
        }
    }
}

fun Parent.polyline(polyline: Polyline, op: (Line.() -> Unit) = { Unit }): Unit {
    group {
        polyline.points.zip(polyline.points.tail())
                .forEach { (a, b) -> line(a.x, a.y, b.x, b.y){
                    fill = Color.gray(0.0, 0.0)
                    op()
                } }
    }
}
