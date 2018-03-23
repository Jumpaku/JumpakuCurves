package jumpaku.examples.blend

import io.vavr.control.Option
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.json.parseJson
import jumpaku.fsc.blend.BlendResult
import jumpaku.fsc.blend.Blender
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fxcomponents.nodes.curveControl
import jumpaku.fxcomponents.nodes.onCurveDone
import jumpaku.fxcomponents.nodes.cubicFsc
import tornadofx.App
import tornadofx.View
import tornadofx.group
import tornadofx.pane
import java.io.File

fun main(vararg args: String) = Application.launch(AppBlend::class.java, *args)

class AppBlend : App(ViewBlend::class)

class ViewBlend : View() {

    val generator = FscGenerator(3, 0.1, generateFuzziness = { crisp, ts ->
        val derivative1 = crisp.derivative
        val derivative2 = derivative1.derivative
        val velocityCoefficient = 0.004
        val accelerationCoefficient = 0.003
        ts.map {
            val v = derivative1(it).length()
            val a = derivative2(it).length()
            velocityCoefficient * v + a * accelerationCoefficient + 1.0
        }
    })

    val blender = Blender(
            1.0/128,
            0.5,
            generator,
            { _ -> grade.value }
    )

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
        existing = existing.flatMap {
            val br = blender.blend(it, overlap)
            br.blended.orElse { Option.of(it) }
        }.orElse { Option.of(overlap) }
        existing.forEach { cubicFsc(it) { stroke = Color.RED } }
    }
}
