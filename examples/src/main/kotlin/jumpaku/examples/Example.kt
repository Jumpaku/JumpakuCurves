package jumpaku.examples

import javafx.application.Application
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.fxcomponents.node.curveControl
import jumpaku.fxcomponents.node.onCurveDone
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
                val (startX, startY) = it.data.head().point
                val (endX, endY) = it.data.last().point
                clear()
                with(group) {
                    children.clear()
                    line(startX, startY, endX, endY) { stroke = Color.ORANGE }
                }
            }
        }
    }
}