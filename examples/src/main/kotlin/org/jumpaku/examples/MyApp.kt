package org.jumpaku.examples

import io.vavr.API.List
import io.vavr.collection.Array
import io.vavr.collection.List
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.paint.Color
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.fitting.BSplineFitting
import org.jumpaku.core.fsci.interpolate
import tornadofx.*


fun main(args: kotlin.Array<String>) = Application.launch(MyApp::class.java, *args)

class MyApp: App(TestView::class)

class TestView : View(){

    override val scope: Scope = Scope()

    val curveInput = CurveInput(scope = scope)

    override val root = curveInput.root

    init {
        subscribe<CurveInput.CurveDoneEvent> {
            with(curveInput.contents){
                children.clear()
                val domain = Interval(it.data.head().time, it.data.last().time)
                BSplineFitting(3, domain, 0.125).fit(interpolate(it.data, 0.03125))
                        .toBeziers()
                        .forEach {
                            cubiccurve {
                                val cp = it.controlPoints
                                stroke = Color.BLUE
                                fill = Color.rgb(0,0,0,0.0)
                                startX = cp[0].x
                                startY = cp[0].y
                                controlX1 = cp[1].x
                                controlY1 = cp[1].y
                                controlX2 = cp[2].x
                                controlY2 = cp[2].y
                                endX = cp[3].x
                                endY = cp[3].y
                            }
                        }
            }
        }
    }

}

class CurveInput(val width: Double = 640.0, val height: Double = 480.0, override val scope: Scope = DefaultScope) : View() {

    class CurveDoneEvent(val data: Array<TimeSeriesPoint>, scope: Scope = DefaultScope) : FXEvent(scope = scope)

    private var points: List<TimeSeriesPoint> = List()

    val polyline = Group()

    val contents = Group()

    val parent = group {
        add(polyline)
        add(contents)
    }

    override val root = pane {
        prefWidth = this@CurveInput.width
        prefHeight = this@CurveInput.height

        add(this@CurveInput.parent)

        setOnMousePressed {
            points = List()
            this@CurveInput.parent.children.map {
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













