package jumpaku.curves.graphics.fx

import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.WindowEvent
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.graphics.DrawStyle
import jumpaku.curves.graphics.clearRect
import jumpaku.curves.graphics.drawGrids
import jumpaku.curves.graphics.drawPolyline
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Rectangle2D

fun main(vararg args: String) = Application.launch(CurveInputApp::class.java, *args)

private val bounds = Rectangle2D.Double(0.0, 0.0, 600.0, 480.0)

private val grid = Grid(100.0, 10.0, 2, Point.xy(300.0, 240.0))

private fun Graphics2D.initialize() {
    drawGrids(grid, 0, bounds)
}

private fun Graphics2D.update(drawingStroke: DrawingStroke) {
    clearRect(bounds)
    drawGrids(grid, 0, bounds)
    drawPolyline(Polyline(drawingStroke.paramPoints), DrawStyle(color = Color.CYAN))
}

class CurveInputApp : Application() {

    override fun start(primaryStage: Stage) {
        val curveControl = DrawingControl(bounds.width, bounds.height).apply {
            addEventHandler(CurveEvent.CURVE_DONE) { e ->
                updateGraphics2D { update(e.drawingStroke) }
            }
        }
        primaryStage.scene = Scene(curveControl)
        primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN) {
            curveControl.updateGraphics2D { initialize() }
        }
        primaryStage.show()
    }

}
