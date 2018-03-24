package jumpaku.examples

import javafx.application.Application
import javafx.scene.layout.Pane
import jumpaku.fxcomponents.nodes.curveControl
import jumpaku.fxcomponents.nodes.onCurveDone
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