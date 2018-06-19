package jumpaku.examples

import javafx.application.Application
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.arclength.Reparametrizer
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.geom.Point
import jumpaku.fxcomponents.nodes.curveControl
import jumpaku.fxcomponents.nodes.fuzzyPoints
import jumpaku.fxcomponents.nodes.onCurveDone
import org.apache.commons.math3.util.FastMath
import tornadofx.App
import tornadofx.View
import tornadofx.group
import tornadofx.pane
import kotlin.math.sqrt


fun main(vararg args: String) = Application.launch(AppExample::class.java, *args)

class AppExample : App(ViewExample::class)

class ViewExample : View() {
    val R2 = sqrt(2.0)
    val l = ConicSection(Point.xy(200.0, 300.0),
            Point.xy(100.0 * (2 - R2 / 2), 100.0 * (2 - R2 / 2)),
            Point.xy(300.0, 200.0),
            -R2 / 2)
    override val root: Pane = pane {
        val group = group {
            Reparametrizer.of(l, l.approximateParams(1.0)).run {
                fuzzyPoints(range.sample(20).map { l(toOriginal(it)).copy(r = 2.0) }) { stroke = Color.BLUE }
            }
        }
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