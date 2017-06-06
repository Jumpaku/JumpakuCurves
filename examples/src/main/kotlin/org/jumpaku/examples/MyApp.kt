package org.jumpaku.examples

import javafx.application.Application
import javafx.scene.paint.Color
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.fitting.BSplineFitting
import org.jumpaku.core.fsci.interpolate
import org.jumpaku.fxcomponents.view.CurveInput
import tornadofx.*


fun main(args: kotlin.Array<String>): Unit = Application.launch(MyApp::class.java, *args)

class MyApp: App(TestView::class)

class TestView : View(){

    override val scope: Scope = Scope()

    val curveInput = CurveInput(scope = scope)

    override val root = curveInput.root

    init {
        subscribe<CurveInput.CurveDoneEvent> {
            with(curveInput.contents){
                val domain = Interval(it.data.head().time, it.data.last().time)
                val b = BSplineFitting(3, domain, 0.125).fit(interpolate(it.data, 0.03125))
                b.toBeziers()
                    .forEach {
                        cubiccurve {
                            val cp = it.controlPoints
                            stroke = Color.BLUE
                            fill = Color.rgb(0,0,0,0.0)
                            startX = cp[0].x
                            startY = cp[0].y
                            controlX1 = cp[1].x
                            controlY1 = cp[1].y
                            controlX2 = cp[2].x
                            controlY2 = cp[2].y
                            endX = cp[3].x
                            endY = cp[3].y
                        }
                    }
                path {
                    stroke = Color.ORANGE
                    moveTo(b.controlPoints[0].x, b.controlPoints[0].y)
                    b.controlPoints.forEach { lineTo(it.x, it.y) }
                }
            }
        }
    }
}
