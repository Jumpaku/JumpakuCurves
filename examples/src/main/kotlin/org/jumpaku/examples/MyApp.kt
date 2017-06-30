package org.jumpaku.examples

import io.vavr.collection.Array
import javafx.application.Application
import javafx.scene.paint.Color
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.fsc.generate.FscGeneration
import org.jumpaku.fsc.identify.Identifier4
import org.jumpaku.fxcomponents.view.CurveInput
import org.jumpaku.fxcomponents.view.cubicFsc
import org.jumpaku.fxcomponents.view.fuzzyCurve
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
        subscribe<CurveInput.CurveDoneEvent> {
            render(it.data)
        }
    }

    private fun render(data: Array<ParamPoint>): Unit {
        with(curveInput.contents) {
            val fsc = FscGeneration(3, 0.1).generate(Array.ofAll(data))
            cubicFsc(fsc) { stroke = Color.RED }
            val result = Identifier4().identify(fsc)
            result.rationalBezier.forEach {
                fuzzyCurve(it) { stroke = Color.BLUE }
            }
            println(result.grades)
        }
    }
}
