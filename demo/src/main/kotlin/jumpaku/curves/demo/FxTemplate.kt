package jumpaku.curves.demo

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.graphics.clearRect
import jumpaku.curves.graphics.drawPolyline
import jumpaku.curves.graphics.fx.*


fun main(vararg args: String) = Application.launch(FxTemplate::class.java, *args)

class FxTemplate : Application() {

    val width = 600.0
    val height = 480.0

    override fun start(primaryStage: Stage) {
        val curveControl = DrawingControl(width, height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) {
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    drawPolyline(Polyline(it.drawingStroke.paramPoints))
                }
            }
        }
        primaryStage.apply {
            scene = Scene(curveControl)
            show()
        }
    }
}