package org.jumpaku.examples

import io.vavr.API
import io.vavr.collection.Array
import javafx.application.Application
import javafx.scene.paint.Color
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Precision
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.WeightedPoint
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.arclength.repeatBisection
import org.jumpaku.core.curve.bezier.Bezier
import org.jumpaku.core.curve.polyline.Polyline
import org.jumpaku.core.curve.rationalbezier.ConicSection
import org.jumpaku.core.curve.rationalbezier.RationalBezier
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
        with(curveInput.contents) {
            val R2 = Math.sqrt(2.0)
            val r = RationalBezier(
                    WeightedPoint(Point.xyr(0.0, 100.0,  1.0),  1.0),
                    WeightedPoint(Point.xyr(100.0, 100.0,  2.0), 1/R2),
                    WeightedPoint(Point.xyr(100.0, 0.0,  3.0),  1.0))
                    .subdivide(0.1)
            val c = ConicSection(
                    Point.xyr(0.0, 100.0, 1.0), Point.xyr(R2/0.02, R2/0.02, 2.0), Point.xyr(100.0, 0.0, 3.0), R2/2)
                    .subdivide(0.1)
            fuzzyPoints(r._2().controlPoints.append(r._2()(0.5))) {stroke = Color.RED}
            fuzzyPoints(c._2().representPoints) {stroke = Color.BLUE}
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
