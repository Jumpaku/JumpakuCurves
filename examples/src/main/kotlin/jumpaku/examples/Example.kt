package jumpaku.examples

import javafx.application.Application
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.fxcomponents.node.curveInputPane
import tornadofx.*


fun main(vararg args: String) = Application.launch(AppExample::class.java, *args)

class AppExample : App(ViewExample::class)

class ViewExample : View() {

    override val scope: Scope = Scope()

    override val root: Pane = curveInputPane(600.0, 600.0) { paramPoints ->
        val (startX, startY) = paramPoints.head().point
        val (endX, endY) = paramPoints.last().point
        children.clear()
        line(startX, startY, endX, endY){
            strokeWidth=10.0
            stroke = Color.ORANGE
        }
    }
}