package org.jumpaku.fxcomponents.view

import javafx.scene.Parent
import javafx.scene.shape.Shape
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.polyline.Polyline
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2
import tornadofx.cubiccurve
import tornadofx.group
import tornadofx.line


fun Parent.cubicBSpline(bSpline: BSpline, op: (Shape.() -> Unit) = { Unit }): Unit {
    group {
        bSpline.toBeziers().forEach {
            val cp = it.controlPoints
            cubiccurve(
                    startX = cp[0].x,
                    startY = cp[0].y,
                    controlX1 = cp[1].x,
                    controlY1 = cp[1].y,
                    controlX2 = cp[2].x,
                    controlY2 = cp[2].y,
                    endX = cp[3].x,
                    endY = cp[3].y,
                    op = op)
        }
    }
}

fun Parent.polyline(polyline: Polyline, op: (Shape.() -> Unit) = { Unit }): Unit {
    group {
        polyline.points.zip(polyline.points.tail())
                .forEach { (a, b) ->
                    line(a.x, a.y, b.x, b.y, op)
                }
    }
}
