package jumpaku.examples

import io.vavr.collection.Array
import javafx.application.Application
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import jumpaku.core.curve.arclength.Reparametrizer
import jumpaku.core.curve.arclength.repeatBisect
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.geom.Point
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fxcomponents.nodes.curveControl
import jumpaku.fxcomponents.nodes.fuzzyPoints
import jumpaku.fxcomponents.nodes.onCurveDone
import org.apache.commons.math3.util.FastMath
import tornadofx.*


fun main(vararg args: String) = Application.launch(AppExample::class.java, *args)

class AppExample : App(ViewExample::class)

class ViewExample : View() {
    override val root: Pane = pane {
        val group = group {}
        curveControl {
            prefWidth = 1280.0
            prefHeight = 720.0
            onCurveDone {
                clear()
            }
        }
    }
}