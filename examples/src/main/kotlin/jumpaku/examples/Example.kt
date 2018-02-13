package jumpaku.examples

import javafx.application.Application
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.affine.Point
import jumpaku.core.affine.WeightedPoint
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.curve.rationalbezier.RationalBezier
import jumpaku.fxcomponents.node.curveControl
import jumpaku.fxcomponents.node.onCurveDone
import jumpaku.fxcomponents.view.fuzzyCurve
import jumpaku.fxcomponents.view.polyline
import tornadofx.*


fun main(vararg args: String) = Application.launch(AppExample::class.java, *args)

class AppExample : App(ViewExample::class)

class ViewExample : View() {

    override val root: Pane = pane {
        val group = group {  }
        curveControl {
            prefWidth = 640.0
            prefHeight = 480.0
            onCurveDone {
                clear()
                with(group) {
                    children.clear()
                }
            }
        }
    }
}