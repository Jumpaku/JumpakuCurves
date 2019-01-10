package jumpaku.examples.freecurve

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.freecurve.Shaper
import jumpaku.fsc.freecurve.Smoother
import jumpaku.fxcomponents.colors.Color
import jumpaku.fxcomponents.colors.CudPalette
import jumpaku.fxcomponents.colors.fx
import jumpaku.fxcomponents.nodes.*
import tornadofx.App
import tornadofx.View
import tornadofx.group
import tornadofx.pane


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
                    update(e.fsc)
                }
            }
        }
    }

    fun Group.update(fsc: BSpline) {
        cubicFsc(fsc) { stroke = Color.rgb(63, 63, 63, 0.3).fx(); strokeWidth = 0.5 }
        val (ts, segment, smooth) = Shaper { it.domain.sample(0.1) }.shape(fsc)
        println(ts.size)
        fuzzyPoints(ts.map(fsc)) { stroke = CudPalette.BLACK.fx() }
        fuzzyPoints(segment.segmentParamIndices.map { fsc(ts[it]) }) { stroke = CudPalette.RED.fx() }
        smooth.conicSections.forEach { curve(it) { stroke = CudPalette.SKY.fx(); strokeWidth = 3.0 } }
        smooth.cubicBeziers.forEach { curve(it) { stroke = CudPalette.ORANGE.fx(); strokeWidth = 3.0 } }
    }
}