package org.jumpaku.fxcomponents.view


import io.vavr.API
import io.vavr.collection.Array
import io.vavr.collection.List
import javafx.scene.Group
import javafx.scene.paint.Color
import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.polyline.Polyline
import tornadofx.*


class CurveInput(val width: Double = 640.0, val height: Double = 480.0, override val scope: Scope = DefaultScope) : View() {

    class CurveDoneEvent(val data: Array<ParamPoint>, scope: Scope = DefaultScope) : FXEvent(scope = scope)

    private var points: List<ParamPoint> = API.List()

    val inputPolyline = Group()

    val contents = Group()

    private val parent = group {
        add(inputPolyline)
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
            points = points.prepend(ParamPoint.now(Point.xy(it.x, it.y)))
            render()
        }
        setOnMouseReleased {
            points = points.prepend(ParamPoint.now(Point.xy(it.x, it.y)))
            fire(CurveDoneEvent(points.toArray().sorted(Comparator.comparing(ParamPoint::param)), scope))
        }
    }

    private fun render() : Unit {
        if(points.size() <= 2){
            return Unit
        }
        with(inputPolyline) {
            children.clear()
            polyline(Polyline(points.map(ParamPoint::point))) {
                stroke = Color.RED
            }
        }
    }
}