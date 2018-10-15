package jumpaku.fxcomponents.nodes

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.event.EventType
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import jumpaku.core.geom.Point
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.polyline.Polyline
import tornadofx.add
import tornadofx.circle
import tornadofx.opcr


fun EventTarget.curveControl(op: (CurveControl.() -> Unit) = {}): CurveControl = opcr(this, CurveControl(), op)
fun CurveControl.onCurveDone(op: (CurveControl.(CurveEvent) -> Unit)){ this.onCurveDone = EventHandler { op(it) } }

private class CurveControlSkin(val control: CurveControl) : Skin<CurveControl> {
    val controller: CurveControl.Controller = CurveControl.Controller(control)
    val inputPolyline = Group()
    val rootNode = Region()

    init {
        with(rootNode){
            add(inputPolyline)
            addEventHandler(MouseEvent.MOUSE_PRESSED) { controller.onPressed() }
            addEventHandler(MouseEvent.MOUSE_DRAGGED) { controller.onDragged(it) }
            addEventHandler(MouseEvent.MOUSE_RELEASED) { controller.onReleased(it) }
        }
    }
    override fun getNode(): Node = rootNode
    override fun dispose() {}
    override fun getSkinnable(): CurveControl = control

    fun render(points: List<ParamPoint>): Unit = with(inputPolyline) {
        children.clear()
        when {
            points.isEmpty() -> Unit
            points.size == 1 -> circle(points[0].point.x, points[0].point.y, 1) { stroke = Color.BLACK }
            else -> polyline(Polyline.of(points.map(ParamPoint::point))) { stroke = Color.BLACK }
        }
    }
}


class CurveEvent(val data: List<ParamPoint>) : Event(CurveEvent.CURVE_DONE) {
    companion object {
        val CURVE_DONE = EventType<CurveEvent>(ANY, "CURVE_DONE")
    }
}

class CurveControl : Control() {

    class Controller(val control: CurveControl) {
        fun onPressed() {
            control.points.clear()
            control.update()
        }
        fun onDragged(e: MouseEvent) {
            control.points.add(ParamPoint.now(Point.xy(e.x, e.y)))
            control.update()
        }
        fun onReleased(e: MouseEvent) {
            control.points.add(ParamPoint.now(Point.xy(e.x, e.y)))
            control.update()
            control.fireEvent(CurveEvent(control.points.sortedWith(Comparator.comparing(ParamPoint::param))))
        }
    }

    private var points: MutableList<ParamPoint> = mutableListOf()

    fun update() = (skin as CurveControlSkin)
            .render(points.sortedWith(Comparator.comparing(ParamPoint::param)))

    override fun createDefaultSkin(): Skin<*> = CurveControlSkin(this)

    fun clear() = (skin as CurveControlSkin).inputPolyline.children.clear()

    private val onCurveDoneProperty: ObjectProperty<EventHandler<CurveEvent>> = object : SimpleObjectProperty<EventHandler<CurveEvent>>(
            this, "onCurveDone", EventHandler { _ -> Unit }) {
        override fun invalidated() = setEventHandler(CurveEvent.CURVE_DONE, get())
    }
    fun onCurveDoneProperty(): ObjectProperty<EventHandler<CurveEvent>> = onCurveDoneProperty
    var onCurveDone: EventHandler<CurveEvent> get() = onCurveDoneProperty().get(); set(h) = onCurveDoneProperty().set(h)
}

