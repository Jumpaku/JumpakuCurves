package org.jumpaku.examples

import io.vavr.collection.Array
import javafx.application.Application
import javafx.scene.paint.Color
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.polyline.Polyline
import org.jumpaku.core.fitting.BSplineFitting
import org.jumpaku.core.fsci.extrapolateBack
import org.jumpaku.core.fsci.extrapolateFront
import org.jumpaku.core.fsci.interpolate
import org.jumpaku.fxcomponents.view.CurveInput
import org.jumpaku.fxcomponents.view.cubicBSpline
import org.jumpaku.fxcomponents.view.polyline
import tornadofx.*


fun main(args: kotlin.Array<String>): Unit = Application.launch(MyApp::class.java, *args)

class MyApp: App(TestView::class)

class TestView : View(){

    override val scope: Scope = Scope()

    val curveInput = CurveInput(scope = scope)

    override val root = curveInput.root

    init {
        subscribe<CurveInput.CurveDoneEvent> {
            val sorted = it.data.sorted(Comparator.comparing(TimeSeriesPoint::time))
            val data = modifyData(sorted)
            val bSpline = BSplineFitting(3, Interval(data.head().time, data.last().time), 0.1)
                    .fit(data).restrict(sorted.head().time, sorted.last().time)
            renderBSpline(bSpline)
        }
    }

    private fun modifyData(data: Array<TimeSeriesPoint>): Array<TimeSeriesPoint> {
        val sorted = data.sorted(Comparator.comparing(TimeSeriesPoint::time))
        val interpolated = interpolate(sorted, 0.1/10)
        val extrapolatedFront = extrapolateFront(interpolated, 0.1)
        val extrapolatedBack = extrapolateBack(extrapolatedFront, 0.1)
        return extrapolatedBack
    }

    private fun renderBSpline(bSpline: BSpline): Unit {
        with(curveInput.contents) {
            cubicBSpline(bSpline) {
                stroke = Color.BLUE
                fill = Color.gray(0.0, 0.0)
            }
            polyline(Polyline(bSpline.controlPoints)){
                stroke = Color.ORANGE
            }
        }
    }
}
