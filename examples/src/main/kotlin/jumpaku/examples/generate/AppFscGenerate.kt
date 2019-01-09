package jumpaku.examples.generate

import javafx.application.Application
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.fsc.generate.DataPreparer
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fsc.generate.LinearFuzzifier
import jumpaku.fxcomponents.nodes.cubicFsc
import jumpaku.fxcomponents.nodes.curveControl
import jumpaku.fxcomponents.nodes.onCurveDone
import tornadofx.App
import tornadofx.View
import tornadofx.group
import tornadofx.pane


fun main(vararg args: String) = Application.launch(AppFscGenerate::class.java, *args)

class AppFscGenerate : App(ViewFscGenerate::class)

class ViewFscGenerate : View() {

    val generator = FscGenerator(
            degree = 3,
            knotSpan = 0.05,
            preparer = DataPreparer(0.05/3, 0.1, 0.1, 2),
            fuzzifier = LinearFuzzifier(0.01, 0.0004))

    override val root: Pane = pane {
        val group = group { }
        curveControl {
            prefWidth = 1280.0
            prefHeight = 720.0
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
