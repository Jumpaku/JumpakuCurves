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
import jumpaku.commons.control.Option
import jumpaku.commons.control.none
import jumpaku.commons.control.result
import jumpaku.commons.control.some
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.polyline.LineSegment
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.graphics.clearRect
import jumpaku.curves.graphics.drawLineSegment
import org.jfree.fx.FXGraphics2D
import java.awt.Color
import java.awt.Graphics2D


private class DrawingControlSkin(val control: DrawingControl, val rootNode: Canvas) : Skin<DrawingControl> {

    val fxGraphics2D: Graphics2D = FXGraphics2D(rootNode.graphicsContext2D)

    init {
        fxGraphics2D.background = Color.WHITE
        fxGraphics2D.clearRect(0.0, 0.0, rootNode.width, rootNode.height)

        fun currentData(point: Point) = ParamPoint(point, System.nanoTime()*1e-9)
        with(rootNode) {
            addEventHandler(MouseEvent.MOUSE_PRESSED) { control.beginCurve(currentData(Point.xy(it.x, it.y))) }
            addEventHandler(MouseEvent.MOUSE_DRAGGED) { control.extendCurve(currentData(Point.xy(it.x, it.y))) }
            addEventHandler(MouseEvent.MOUSE_RELEASED) { control.endCurve(currentData(Point.xy(it.x, it.y))) }
        }
    }

    override fun getNode(): Node = rootNode

    override fun dispose() {}

    override fun getSkinnable(): DrawingControl = control

    fun renderCurve(drawingStroke: DrawingStroke) {
        val points = drawingStroke.inputData.map { it.point }
        if (points.size >= 2) result {
            val (p0, p1) = points.takeLast(2)
            fxGraphics2D.drawLineSegment(LineSegment(p0, p1))
        }
    }
}


class DrawingEvent(val drawingStroke: DrawingStroke) : Event(DrawingEvent.DRAWING_DONE) {
    companion object {
        val DRAWING_DONE = EventType<DrawingEvent>(ANY, "DRAWING_DONE")
    }
}

private typealias DrawingEventHandlerProperty = SimpleObjectProperty<EventHandler<DrawingEvent>>

class DrawingControl(val canvasWidth: Double, val canvasHeight: Double) : Control() {

    override fun createDefaultSkin(): Skin<*> = DrawingControlSkin(this, Canvas(canvasWidth, canvasHeight))

    private fun updateCurve(drawingStroke: DrawingStroke): Unit = (skin as DrawingControlSkin).renderCurve(drawingStroke)

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
                fireEvent(DrawingEvent(it))
            }
        }
    }

    fun updateGraphics2D(update: Graphics2D.() -> Unit) = ((skin as DrawingControlSkin).fxGraphics2D).update()

    private val onDrawingDoneProperty: ObjectProperty<EventHandler<DrawingEvent>> =
            object : DrawingEventHandlerProperty(this, "onDrawingDone", EventHandler {}) {
                override fun invalidated() = setEventHandler(DrawingEvent.DRAWING_DONE, get())
            }
    fun onCurveDoneProperty(): ObjectProperty<EventHandler<DrawingEvent>> = onDrawingDoneProperty
    var onDrawingDone: EventHandler<DrawingEvent>
        get() = onCurveDoneProperty().get()
        set(h) = onCurveDoneProperty().set(h)
}
