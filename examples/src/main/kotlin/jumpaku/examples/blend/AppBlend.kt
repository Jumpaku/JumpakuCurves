package jumpaku.examples.blend

import io.vavr.control.Option
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.blend.Blender
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fxcomponents.node.curveControl
import jumpaku.fxcomponents.node.onCurveDone
import jumpaku.fxcomponents.view.cubicFsc
import tornadofx.App
import tornadofx.View
import tornadofx.group
import tornadofx.pane

fun main(vararg args: String) = Application.launch(AppBlend::class.java, *args)

class AppBlend : App(ViewBlend::class)

class ViewBlend : View() {

    override val root: Pane = pane {
        val group = group {  }
        curveControl {
            prefWidth = 1280.0
            prefHeight = 720.0
            onCurveDone { e ->
                clear()
                group.update(FscGenerator().generate(e.data))
            }
        }
    }

    var existing: Option<BSpline> = Option.none()

    fun Group.update(overlap: BSpline){
        children.clear()
        existing.forEach { cubicFsc(it) { stroke = Color.BLACK } }
        cubicFsc(overlap) { stroke = Color.BLUE; strokeWidth = 1.0 }
        existing = existing.flatMap { Blender().blend(it, overlap).blended.orElse { Option.of(it) } }.orElse { Option.of(overlap) }
        existing.forEach { cubicFsc(it) { stroke = Color.RED } }
    }
}
