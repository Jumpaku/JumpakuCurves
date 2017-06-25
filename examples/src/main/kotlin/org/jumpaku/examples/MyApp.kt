package org.jumpaku.examples

import io.vavr.collection.Array
import javafx.application.Application
import javafx.scene.paint.Color
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.arclength.ArcLengthAdapter
import org.jumpaku.core.fsci.FscGeneration
import org.jumpaku.core.fsci.reference.Circular
import org.jumpaku.core.fsci.reference.Elliptic
import org.jumpaku.core.fsci.reference.Linear
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
        subscribe<CurveInput.CurveDoneEvent> {
            render(it.data)
        }
    }

    private fun render(data: Array<ParamPoint>): Unit {
        if(data.size() < 2){
            //return
        }
        with(curveInput.contents) {
            FscGeneration(3, 0.1).generate(Array.ofAll(data))
                    .run {
                        cubicFsc(this) { stroke = Color.RED }

                        Elliptic.create(this.domain.begin, this.domain.end, this).toArcLengthCurve()
                                .run { polyline(polyline) { stroke = Color.GREEN } }
                    }
        }
    }
}
