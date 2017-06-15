package org.jumpaku.examples

import io.vavr.API
import io.vavr.collection.Array
import javafx.application.Application
import javafx.scene.paint.Color
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.affine.TimeSeriesPointJson
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.Knot
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.polyline.Polyline
import org.jumpaku.core.fitting.BSplineFitting
import org.jumpaku.core.fsci.DataPreparing
import org.jumpaku.core.fsci.FscGeneration
import org.jumpaku.core.json.prettyGson
import org.jumpaku.fxcomponents.view.*
import tornadofx.App
import tornadofx.Scope
import tornadofx.View
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter


fun main(args: kotlin.Array<String>): Unit = Application.launch(MyApp::class.java, *args)

class MyApp: App(TestView::class)

class TestView : View(){

    override val scope: Scope = Scope()

    val curveInput = CurveInput(scope = scope)

    override val root = curveInput.root

    init {
        subscribe<CurveInput.CurveDoneEvent> {
            render(it.data)
        }
    }

    private fun render(data: Array<TimeSeriesPoint>): Unit {
        val fsc = FscGeneration(3, 0.1).generate(data)
        File("./FscGenerationData.json").writeText(prettyGson.toJson(data.map(TimeSeriesPoint::json).toJavaList()))
        File("./FscGenerationFsc.json").writeText(fsc.toString())

        with(curveInput.contents) {
            cubicFsc(fsc){
                stroke = Color.BLUE
            }
        }
    }
}
