package jumpaku.fxcomponents.node

import io.vavr.collection.Array
import javafx.event.EventTarget
import javafx.scene.Group
import javafx.scene.layout.Pane
import jumpaku.core.curve.ParamPoint
import tornadofx.add
import tornadofx.group

fun EventTarget.curveInputPane(
        width: Double = 640.0, height: Double = 480.0, handler: (Group.(Array<ParamPoint>) -> Unit) = {}): CurveInputPane {
    val pane = CurveInputPane(width, height, handler)
    add(pane)
    return pane
}

class CurveInputPane(width: Double = 640.0, height: Double = 480.0, handler: Group.(Array<ParamPoint>) -> Unit = {}) : Pane() {
    val contents = group()
    init {
        curveControl {
            prefWidth = width
            prefHeight = height
            onCurveDone {
                clear()
                handler(contents, it.data)
            }
        }
    }
}