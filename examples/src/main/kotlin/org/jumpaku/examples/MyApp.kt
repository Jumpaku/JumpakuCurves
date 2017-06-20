package org.jumpaku.examples

import io.vavr.collection.Array
import javafx.application.Application
import org.jumpaku.core.fitting.ParamPoint
import org.jumpaku.fxcomponents.view.*
import tornadofx.App
import tornadofx.Scope
import tornadofx.View


fun main(args: kotlin.Array<String>): Unit = Application.launch(MyApp::class.java, *args)

class MyApp: App(TestView::class)

class TestView : View(){

    override val scope: Scope = Scope()

    val curveInput = CurveInput(scope = scope)

    override val root = curveInput.root

    init {
        //val data = prettyGson.fromJson<List<ParamPointJson>>(FileReader("./Data.json")).map(ParamPointJson::paramPoint)
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

    private fun render(data: Array<ParamPoint>): Unit {

        //File("./FscGenerationFsc.json").writeText(fsc.toString())
    }
}
