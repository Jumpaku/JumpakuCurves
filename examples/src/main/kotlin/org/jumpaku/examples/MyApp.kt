package org.jumpaku.examples

import io.vavr.collection.Array
import javafx.application.Application
import javafx.scene.paint.Color
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.ParamPointJson
import org.jumpaku.core.curve.bspline.BSplineJson
import org.jumpaku.core.json.prettyGson
import org.jumpaku.fsc.generate.FscGenerator
import org.jumpaku.fsc.identify.classify.ClassifierOpen4
import org.jumpaku.fsc.identify.classify.ClassifierPrimitive7
import org.jumpaku.fsc.identify.reference.Circular
import org.jumpaku.fsc.identify.reference.Elliptic
import org.jumpaku.fsc.identify.reference.Linear
import org.jumpaku.fsc.identify.reference.mostFarPointOnFsc
import org.jumpaku.fxcomponents.view.CurveInput
import org.jumpaku.fxcomponents.view.cubicFsc
import org.jumpaku.fxcomponents.view.fuzzyCurve
import tornadofx.App
import tornadofx.Scope
import tornadofx.View
import java.nio.file.Path
import java.nio.file.Paths


fun main(args: kotlin.Array<String>): Unit {
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

    private fun render(data: Array<ParamPoint>): Unit {
        with(curveInput.contents) {
            val fsc = FscGenerator(3, 0.1).generate(Array.ofAll(data))
            cubicFsc(fsc) { stroke = Color.BLUE }
            fuzzyCurve(Linear.of(fsc).fuzzyCurve) { stroke = Color.GREEN }
            fuzzyCurve(Circular.of(fsc).fuzzyCurve) { stroke = Color.RED }
            fuzzyCurve(Elliptic.of(fsc).fuzzyCurve) { stroke = Color.SKYBLUE }
            val result = ClassifierOpen4().classify(fsc)
            println(result.grades)
        }
    }
}
