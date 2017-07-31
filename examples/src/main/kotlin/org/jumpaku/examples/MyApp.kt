package org.jumpaku.examples

import io.vavr.API
import io.vavr.collection.Array
import javafx.application.Application
import javafx.scene.paint.Color
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Precision
import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.arclength.repeatBisection
import org.jumpaku.core.curve.bezier.Bezier
import org.jumpaku.core.curve.polyline.Polyline
import org.jumpaku.core.curve.rationalbezier.ConicSection
import org.jumpaku.fsc.generate.FscGenerator
import org.jumpaku.fsc.identify.classify.ClassifierOpen4
import org.jumpaku.fsc.identify.reference.Circular
import org.jumpaku.fsc.identify.reference.Elliptic
import org.jumpaku.fsc.identify.reference.Linear
import org.jumpaku.fxcomponents.view.CurveInput
import org.jumpaku.fxcomponents.view.cubicFsc
import org.jumpaku.fxcomponents.view.fuzzyCurve
import org.jumpaku.fxcomponents.view.fuzzyPoints
import tornadofx.App
import tornadofx.Scope
import tornadofx.View


fun main(args: kotlin.Array<String>): Unit {
    Application.launch(MyApp::class.java, *args)
}

class MyApp: App(TestView::class)

class TestView : View(){

    override val scope: Scope = Scope()

    val curveInput = CurveInput(scope = scope)

    override val root = curveInput.root

    init {
        val R2 = FastMath.sqrt(2.0)
        val x = ConicSection(Point.xy(200.0, 300.0),
                Point.xy(100.0*(2 - R2/2), 100.0*(2 - R2/2)),
                Point.xy(300.0, 200.0),
                -R2/2)
        println(x.subdivide(0.1))
        with(curveInput.contents){
            fuzzyPoints(x.toArcLengthCurve().evaluateAll(100)) { stroke = Color.RED }
            fuzzyPoints(x.subdivide(0.9)._2.representPoints) { stroke = Color.BLUE }
            fuzzyPoints(x.restrict(0.3, 0.5).representPoints) { stroke = Color.GREEN }
        }
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
