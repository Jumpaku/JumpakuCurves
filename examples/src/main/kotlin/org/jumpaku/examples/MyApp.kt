package org.jumpaku.examples

import com.github.salomonbrys.kotson.fromJson
import io.vavr.collection.Array
import io.vavr.control.Try
import javafx.application.Application
import javafx.scene.paint.Color
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.bspline.BSplineJson
import org.jumpaku.core.json.prettyGson
import org.jumpaku.fsc.generate.FscGenerator
import org.jumpaku.fsc.identify.classify.ClassifierOpen4
import org.jumpaku.fsc.identify.classify.ClassifierPrimitive7
import org.jumpaku.fsc.identify.reference.Circular
import org.jumpaku.fsc.identify.reference.Elliptic
import org.jumpaku.fsc.identify.reference.Linear
import org.jumpaku.fxcomponents.view.CurveInput
import org.jumpaku.fxcomponents.view.cubicFsc
import org.jumpaku.fxcomponents.view.fuzzyCurve
import tornadofx.App
import tornadofx.Scope
import tornadofx.View
import java.io.File


fun main(args: kotlin.Array<String>) {
    Application.launch(MyApp::class.java, *args)
}

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

    private fun render(data: Array<ParamPoint>) {
        if (data.size() <= 2){
            return
        }
        with(curveInput.contents) {
            val fsc = FscGenerator(3, 0.1).generate(Array.ofAll(data))//prettyGson.fromJson<BSplineJson>(File("/Users/jumpaku/Documents/fsc.json").readText()).bSpline()//
            cubicFsc(fsc) { stroke = Color.BLUE }
            Try.run {
                fuzzyCurve(Linear.ofBeginEnd(fsc).reference) { stroke = Color.GREEN }
                fuzzyCurve(Circular.ofBeginEnd(fsc).reference) { stroke = Color.RED }
                fuzzyCurve(Elliptic.ofBeginEnd(fsc).reference) { stroke = Color.SKYBLUE }
                val result = ClassifierOpen4().classify(fsc)
                println(result.grades)
            }.onFailure {
                println(fsc)
            }
        }
    }
}
