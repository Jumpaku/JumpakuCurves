package org.jumpaku.examples

import io.vavr.collection.Array
import javafx.application.Application
import javafx.scene.paint.Color
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.fsc.generate.FscGenerator
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
import java.nio.file.Paths


fun main(args: kotlin.Array<String>): Unit = Application.launch(MyApp::class.java, *args)

class MyApp: App(TestView::class)

class TestView : View(){

    override val scope: Scope = Scope()

    val curveInput = CurveInput(scope = scope)

    override val root = curveInput.root

    init {
        println(Paths.get(".").toAbsolutePath())
        subscribe<CurveInput.CurveDoneEvent> {
            render(it.data)
        }
    }

    private fun render(data: Array<ParamPoint>): Unit {
        with(curveInput.contents) {
            val fsc = FscGenerator(3, 0.1).generate(Array.ofAll(data))
            cubicFsc(fsc) { stroke = Color.BLUE }
            val t0 = fsc.domain.begin
            val t1 = mostFarPointOnFsc(t0, fsc)
            fuzzyCurve(Linear.create(t0, fsc.domain.end, fsc).fuzzyCurve) { stroke = Color.GREEN }
            fuzzyCurve(Circular.create(t0, t1, fsc).fuzzyCurve) { stroke = Color.RED }
            fuzzyCurve(Elliptic.create(t0, t1, fsc).fuzzyCurve) { stroke = Color.SKYBLUE }
            val result = ClassifierPrimitive7().classify(fsc)
            println(result.grades)
        }
    }
}
