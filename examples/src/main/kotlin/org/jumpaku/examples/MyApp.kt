package org.jumpaku.examples

import io.vavr.collection.Array
import javafx.application.Application
import javafx.scene.paint.Color
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.fsc.fragment.Fragmentation
import org.jumpaku.fsc.generate.FscGeneration
import org.jumpaku.fxcomponents.view.CurveInput
import org.jumpaku.fxcomponents.view.cubicFsc
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
            val fragments = Fragmentation().fragment(fsc)
            val
            //cubicFsc(fsc) { stroke = Color.LIGHTGREEN }
            for ((i, f) in fragments.withIndex()) {
                cubicFsc(f) { stroke = if (i % 2 == 0) Color.LIGHTBLUE else Color.ORANGE }
            }
        }
    }
}
