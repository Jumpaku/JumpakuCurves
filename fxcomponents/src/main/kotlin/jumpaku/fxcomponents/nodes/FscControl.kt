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
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.geom.Point
import jumpaku.core.util.Option
import jumpaku.core.util.optionWhen
import jumpaku.fsc.generate.FscGenerator
import tornadofx.add
import tornadofx.circle
import tornadofx.opcr


fun EventTarget.fscControl(fscGenerator: FscGenerator = FscGenerator(), op: (FscControl.() -> Unit) = {}): FscControl =
        opcr(this, FscControl(fscGenerator), op)
fun FscControl.onFscDone(op: (FscControl.(FscEvent) -> Unit)){ this.onFscDone = EventHandler { op(it) } }

private class FscControlSkin(val control: FscControl) : Skin<FscControl> {
    val controller: FscControl.Controller = FscControl.Controller(control)
    val inputPolyline = Group()
    val rootNode = Region()

    init {
        with(rootNode){
            add(inputPolyline)
            addEventHandler(MouseEvent.MOUSE_PRESSED) { controller.onPressed(it)}
            addEventHandler(MouseEvent.MOUSE_DRAGGED) { controller.onDragged(it) }
            addEventHandler(MouseEvent.MOUSE_RELEASED) { controller.onReleased(it) }
        }
    }
    override fun getNode(): Node = rootNode
    override fun dispose() {}
    override fun getSkinnable(): FscControl = control

    fun render(points: List<ParamPoint>): Unit = with(inputPolyline) {
        children.clear()
        when {
            points.isEmpty() -> Unit
            points.size == 0 -> circle(points[0].point.x, points[0].point.y, 1) { stroke = Color.BLACK }
            else -> polyline(Polyline.of(points.map(ParamPoint::point))) { stroke = Color.BLACK }
        }
    }
}


class FscEvent(val fsc: BSpline) : Event(FscEvent.FSC_DONE) {
    companion object {
        val FSC_DONE = EventType<FscEvent>(ANY, "FSC_DONE")
    }
}

class FscControl(val fscGenerator: FscGenerator) : Control() {

    class Controller(val control: FscControl) {
        fun onPressed(e: MouseEvent) = control.clearData()
        fun onDragged(e: MouseEvent) = control.addData(ParamPoint.now(Point.xy(e.x, e.y)))
        fun onReleased(e: MouseEvent) {
            control.addData(ParamPoint.now(Point.xy(e.x, e.y)))
            control.generateFsc().forEach {
                control.fireEvent(FscEvent(it))
            }
            control.clearData()
        }
    }

    private var data: MutableList<ParamPoint> = mutableListOf()

    fun clearData() {
        data.clear()
        update()
    }

    fun addData(d: ParamPoint) {
        data.add(d)
        update()
    }

    fun generateFsc(): Option<BSpline> = optionWhen(data.size >= 2) {
        fscGenerator.generate(data.toList())
    }

    fun update(): Unit = (skin as FscControlSkin).render(data.sortedWith(Comparator.comparing(ParamPoint::param)))

    override fun createDefaultSkin(): Skin<*> = FscControlSkin(this)

    private val onFscDoneProperty: ObjectProperty<EventHandler<FscEvent>> =
            object : SimpleObjectProperty<EventHandler<FscEvent>>(
                    this, "onFscDone", EventHandler { _ -> Unit }) {
                override fun invalidated() = setEventHandler(FscEvent.FSC_DONE, get())
            }
    fun onFscDoneProperty(): ObjectProperty<EventHandler<FscEvent>> = onFscDoneProperty
    var onFscDone: EventHandler<FscEvent> get() = onFscDoneProperty().get(); set(h) = onFscDoneProperty().set(h)
}
