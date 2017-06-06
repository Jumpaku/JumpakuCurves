package org.jumpaku.fxcomponents.view


import io.vavr.API
import io.vavr.collection.Array
import io.vavr.collection.List
import javafx.scene.Group
import javafx.scene.paint.Color
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.TimeSeriesPoint
import tornadofx.*


class CurveInput(val width: Double = 640.0, val height: Double = 480.0, override val scope: Scope = DefaultScope) : View() {

    class CurveDoneEvent(val data: Array<TimeSeriesPoint>, scope: Scope = DefaultScope) : FXEvent(scope = scope)

    private var points: List<TimeSeriesPoint> = API.List()

    val polyline = Group()

    val contents = Group()

    private val parent = group {
        add(polyline)
        add(contents)
    }

    override val root = pane {
        prefWidth = this@CurveInput.width
        prefHeight = this@CurveInput.height

        add(this@CurveInput.parent)

        setOnMousePressed {
            points = API.List()
            this@CurveInput.parent.children.forEach {
                if(it is Group){
                    it.children.clear()
                }
            }
            render()
        }
        setOnMouseDragged {
            points = points.prepend(TimeSeriesPoint(Point.xy(it.x, it.y)))
            render()
        }
        setOnMouseReleased {
            points = points.prepend(TimeSeriesPoint(Point.xy(it.x, it.y)))
            fire(CurveDoneEvent(points.toArray().sorted(Comparator.comparing(TimeSeriesPoint::time)), scope))
        }
    }

    private fun render() : Unit {
        with(polyline) {
            val ps = points.reverse().map(TimeSeriesPoint::point).toArray()
            children.clear()
            if(!points.isEmpty) {
                path {
                    stroke = Color.RED
                    moveTo(ps[0].x, ps[0].y)
                    ps.forEach { lineTo(it.x, it.y) }
                }
            }
        }
    }
}