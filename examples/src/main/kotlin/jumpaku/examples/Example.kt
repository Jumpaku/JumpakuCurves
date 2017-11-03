package jumpaku.examples

import javafx.application.Application
import javafx.scene.layout.Pane
import tornadofx.App
import tornadofx.Scope
import tornadofx.View
import tornadofx.pane


fun main(vararg args: String) = Application.launch(AppExample::class.java, *args)

class AppExample : App(ViewExample::class)

class ViewExample : View() {

    override val scope: Scope = Scope()

    override val root: Pane = pane {
        prefWidth = 640.0
        prefHeight = 480.0
    }
}