package org.jumpaku.examples

import com.github.salomonbrys.kotson.fromJson
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
import org.jumpaku.core.curve.bspline.BSplineJson
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
import java.io.FileReader
import java.io.FileWriter
import java.io.PrintWriter


fun main(args: kotlin.Array<String>): Unit = Application.launch(MyApp::class.java, *args)

class MyApp: App(TestView::class)

class TestView : View(){

    override val scope: Scope = Scope()

    val curveInput = CurveInput(scope = scope)

    override val root = curveInput.root

    init {
        //val data = prettyGson.fromJson<List<TimeSeriesPointJson>>(FileReader("./Data.json")).map(TimeSeriesPointJson::timeSeriesPoint)
        //val fsc = FscGeneration(3, 0.1).generate(Array.ofAll(data))
        //FileWriter("./Fsc.json").use { prettyGson.toJson(fsc.json(), it) }
        //prettyGson.fromJson<BSplineJson>(FileReader("./Fsc"))
        //with(curveInput.contents) {
            //cubicFsc(prettyGson.fromJson<BSplineJson>(FileReader("./Fsc.json")).bSpline()) {
              //  stroke = Color.BLUE
            //}
        //}
        subscribe<CurveInput.CurveDoneEvent> {
            render(it.data)
        }
    }

    private fun render(data: Array<TimeSeriesPoint>): Unit {

        //File("./FscGenerationFsc.json").writeText(fsc.toString())
    }
}
