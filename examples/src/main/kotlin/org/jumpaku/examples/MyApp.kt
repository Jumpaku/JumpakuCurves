package org.jumpaku.examples

import javafx.application.Application
import javafx.scene.paint.Color
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.polyline.Polyline
import org.jumpaku.core.fsci.FscGeneration
import org.jumpaku.fxcomponents.view.CurveInput
import org.jumpaku.fxcomponents.view.cubicBSpline
import org.jumpaku.fxcomponents.view.fuzzyPoint
import org.jumpaku.fxcomponents.view.polyline
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
            renderBSpline(FscGeneration(3, 0.1).generate(it.data))
        }
    }


    private fun renderBSpline(bSpline: BSpline): Unit {
        with(curveInput.contents) {
            cubicBSpline(bSpline) {
                stroke = Color.BLUE
                fill = Color.gray(0.0, 0.0)
            }
            bSpline.domain.sample(0.01).map(bSpline)
                    .forEach {
                        fuzzyPoint(it) {
                            stroke = Color.BLUE
                            fill = Color.gray(0.0, 0.0)
                        }
                    }
            polyline(Polyline(bSpline.controlPoints)){
                stroke = Color.ORANGE
            }
        }
    }
}
