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
import jumpaku.core.util.*
import jumpaku.fsc.blend.Blender
import jumpaku.fsc.generate.Generator
import tornadofx.add
import tornadofx.circle
import tornadofx.opcr


fun EventTarget.fscUpdateControl(
        fscGenerator: Generator = Generator(),
        blender: Blender = Blender(),
        op: (FscUpdateControl.() -> Unit) = {}): FscUpdateControl =
        opcr(this, FscUpdateControl(fscGenerator, blender), op)
fun FscUpdateControl.onFscUpdated(op: (FscUpdateControl.(FscUpdateEvent) -> Unit)){ this.onFscUpdated = EventHandler { op(it) } }

private class FscUpdateControlSkin(val control: FscUpdateControl) : Skin<FscUpdateControl> {
    val controller: FscUpdateControl.Controller = FscUpdateControl.Controller(control)
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
    override fun getSkinnable(): FscUpdateControl = control

    fun render(data: List<ParamPoint>): Unit = with(inputPolyline) {
        children.clear()
        when {
            data.isEmpty() -> Unit
            data.size == 1 -> circle(data[0].point.x, data[0].point.y, 1) { stroke = Color.BLACK }
            else -> polyline(Polyline.of(data.map(ParamPoint::point))) { stroke = Color.BLACK }
        }
    }
}


class FscUpdateEvent(val fsc: BSpline) : Event(FscUpdateEvent.FSC_UPDATED) {
    companion object {
        val FSC_UPDATED = EventType<FscUpdateEvent>(ANY, "FSC_UPDATED")
    }
}

class FscUpdateControl(val fscGenerator: Generator, val blender: Blender) : Control() {

    class Controller(val control: FscUpdateControl) {
        fun onPressed(e: MouseEvent) = control.clearData()
        fun onDragged(e: MouseEvent) = control.addData(ParamPoint.now(Point.xy(e.x, e.y)))
        fun onReleased(e: MouseEvent) {
            control.addData(ParamPoint.now(Point.xy(e.x, e.y)))
            control.attemptUpdateFsc().forEach {
                control.fireEvent(FscUpdateEvent(it))
            }
            control.clearData()
        }
    }

    private var data: MutableList<ParamPoint> = mutableListOf()

    private var existingFsc: Option<BSpline> = none()

    fun clearData() {
        data.clear()
        update()
    }

    fun addData(d: ParamPoint) {
        data.add(d)
        update()
    }

    fun attemptUpdateFsc(): Option<BSpline> {
        if (data.size < 2) return none()

        val overlap = fscGenerator.generate(data)
        when (existingFsc) {
            is None -> {
                existingFsc = some(overlap)
                return existingFsc
            }
            is Some -> {
                val exist = existingFsc.orThrow()
                val data = blender.blend(exist, overlap)
                data.forEach {
                    existingFsc = some(fscGenerator.generate(it))
                    return existingFsc
                }
            }
            }
        return none()
    }

    fun update() = (skin as FscUpdateControlSkin)
            .render(data.sortedWith(Comparator.comparing(ParamPoint::param)))

    override fun createDefaultSkin(): Skin<*> = FscUpdateControlSkin(this)

    private val onFscUpdatedProperty: ObjectProperty<EventHandler<FscUpdateEvent>> =
            object : SimpleObjectProperty<EventHandler<FscUpdateEvent>>(
            this, "onFscUpdated", EventHandler { _ -> Unit }) {
                override fun invalidated() = setEventHandler(FscUpdateEvent.FSC_UPDATED, get())
            }
    fun onFscUpdatedProperty(): ObjectProperty<EventHandler<FscUpdateEvent>> = onFscUpdatedProperty
    var onFscUpdated: EventHandler<FscUpdateEvent> get() = onFscUpdatedProperty().get(); set(h) = onFscUpdatedProperty().set(h)
}
