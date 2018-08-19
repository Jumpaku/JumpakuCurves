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
import jumpaku.fxcomponents.nodes.*
import org.apache.commons.math3.util.FastMath
import tornadofx.*


fun main(vararg args: String) = Application.launch(AppExample::class.java, *args)

class AppExample : App(ViewExample::class)

class ViewExample : View() {
    override val root: Pane = pane {
        val group = group {}
        fscUpdateControl {
            prefWidth = 1280.0
            prefHeight = 720.0
            onFscUpdated { e ->
                with(group) {
                    children.clear()
                    val s = e.fsc
                    curve(s) { stroke = CudPalette.RED }
                    fuzzyCurve(s) { stroke = CudPalette.BLUE }
                }
            }
        }
    }
}