package jumpaku.curves.graphics.fx

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.input.MouseEvent
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.geom.Line
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.util.Option
import jumpaku.curves.core.util.none
import jumpaku.curves.core.util.result
import jumpaku.curves.core.util.some
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.graphics.clearRect
import jumpaku.curves.graphics.drawLine
import org.jfree.fx.FXGraphics2D
import java.awt.Color
import java.awt.Graphics2D




private class CurveControlSkin(val control: DrawingControl, val rootNode: Canvas) : Skin<DrawingControl> {

    val fxGraphics2D: Graphics2D = FXGraphics2D(rootNode.graphicsContext2D)

    init {
        fxGraphics2D.background = Color.WHITE
        fxGraphics2D.clearRect(0.0, 0.0, rootNode.width, rootNode.height)

        fun currentData(point: Point) = ParamPoint(point, System.nanoTime()*1e-9)
        with(rootNode) {
            // controllers
            addEventHandler(MouseEvent.MOUSE_PRESSED) { control.beginCurve(currentData(Point.xy(it.x, it.y))) }
            addEventHandler(MouseEvent.MOUSE_DRAGGED) { control.extendCurve(currentData(Point.xy(it.x, it.y))) }
            addEventHandler(MouseEvent.MOUSE_RELEASED) { control.endCurve(currentData(Point.xy(it.x, it.y))) }
        }
    }

    override fun getNode(): Node = rootNode

    override fun dispose() {}

    override fun getSkinnable(): DrawingControl = control

    fun renderCurve(drawingStroke: DrawingStroke) {
        val points = drawingStroke.paramPoints.map { it.point }
        if (points.size >= 2) result {
            val (p0, p1) = points.takeLast(2)
            fxGraphics2D.drawLine(Line(p0, p1))
        }
    }
}


class CurveEvent(val drawingStroke: DrawingStroke) : Event(CurveEvent.CURVE_DONE) {
    companion object {
        val CURVE_DONE = EventType<CurveEvent>(ANY, "CURVE_DONE")
    }
}

class DrawingControl(val canvasWidth: Double, val canvasHeight: Double) : Control() {

    private fun updateCurve(drawingStroke: DrawingStroke): Unit = (skin as CurveControlSkin).renderCurve(drawingStroke)

    private val lock: Any = Any()
    private var drawingStroke: Option<DrawingStroke> = none()

    fun beginCurve(point: ParamPoint) {
        synchronized(lock) {
            drawingStroke = some(DrawingStroke(listOf(point)))
            drawingStroke.forEach { updateCurve(it) }
        }
    }

    fun extendCurve(point: ParamPoint) {
        synchronized(lock) {
            require(drawingStroke.isDefined && point.param >= drawingStroke.orThrow().endParam)
            drawingStroke = drawingStroke.map { it.extend(point) }
            drawingStroke.forEach { updateCurve(it) }
        }
    }

    fun endCurve(point: ParamPoint) {
        synchronized(lock) {
            require(drawingStroke.isDefined && point.param >= drawingStroke.orThrow().endParam)
            drawingStroke = drawingStroke.map { it.extend(point) }
            drawingStroke.forEach {
                updateCurve(it)
                fireEvent(CurveEvent(it))
            }
        }
    }

    fun updateGraphics2D(update: Graphics2D.() -> Unit) = ((skin as CurveControlSkin).fxGraphics2D).update()

    override fun createDefaultSkin(): Skin<*> = CurveControlSkin(this, Canvas(canvasWidth, canvasHeight))

    private val onCurveDoneProperty: ObjectProperty<EventHandler<CurveEvent>> =
            object : SimpleObjectProperty<EventHandler<CurveEvent>>(this, "onCurveDone", EventHandler {}) {
                override fun invalidated() = setEventHandler(CurveEvent.CURVE_DONE, get())
            }
    fun onCurveDoneProperty(): ObjectProperty<EventHandler<CurveEvent>> = onCurveDoneProperty
    var onCurveDone: EventHandler<CurveEvent>
        get() = onCurveDoneProperty().get()
        set(h) = onCurveDoneProperty().set(h)
}

