package jumpaku.examples.generate

import javafx.application.Application
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fxcomponents.node.curveControl
import jumpaku.fxcomponents.node.onCurveDone
import jumpaku.fxcomponents.view.cubicFsc
import tornadofx.App
import tornadofx.View
import tornadofx.group
import tornadofx.pane


fun main(vararg args: String) = Application.launch(AppFscGenerate::class.java, *args)

class AppFscGenerate : App(ViewFscGenerate::class)

class ViewFscGenerate : View() {

    val generator = FscGenerator(degree = 3, knotSpan = 0.1, generateFuzziness = { crisp, ts ->
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

    override val root: Pane = pane {
        val group = group { }
        curveControl {
            prefWidth = 640.0
            prefHeight = 480.0
            onCurveDone {
                clear()
                with(group) {
                    children.clear()
                    val fsc = generator.generate(it.data)
                    cubicFsc(fsc) { stroke = Color.RED }

                }
            }
        }
    }
}
