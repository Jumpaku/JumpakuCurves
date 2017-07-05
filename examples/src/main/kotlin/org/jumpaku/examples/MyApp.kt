package org.jumpaku.examples

import com.github.salomonbrys.kotson.fromJson
import io.vavr.collection.Array
import javafx.application.Application
import javafx.scene.paint.Color
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.bspline.BSplineJson
import org.jumpaku.core.json.prettyGson
import org.jumpaku.fsc.generate.FscGenerator
import org.jumpaku.fsc.identify.classify.Classifier4
import org.jumpaku.fsc.identify.reference.Circular
import org.jumpaku.fsc.identify.reference.Elliptic
import org.jumpaku.fsc.identify.reference.EllipticJson
import org.jumpaku.fxcomponents.view.CurveInput
import org.jumpaku.fxcomponents.view.cubicFsc
import org.jumpaku.fxcomponents.view.fuzzyCurve
import tornadofx.App
import tornadofx.Scope
import tornadofx.View
import java.io.FileReader
import java.nio.file.Paths


fun main(args: kotlin.Array<String>): Unit = Application.launch(MyApp::class.java, *args)

class MyApp: App(TestView::class)

class TestView : View(){

    override val scope: Scope = Scope()

    val curveInput = CurveInput(scope = scope)

    override val root = curveInput.root

    init {
        println(Paths.get(".").toAbsolutePath())
        /*val path = Paths.get("./fsc/src/test/resources/org/jumpaku/fsc/identify/reference/")
        val i = 5
        val fsc = FileReader(path.resolve("Fsc$i.json").toFile()).use { prettyGson.fromJson<BSplineJson>(it).bSpline() }
        val arcLength = fsc.toArcLengthCurve()
        val t0 = arcLength.toOriginalParam(arcLength.arcLength() / 5)
        val t1 = arcLength.toOriginalParam(arcLength.arcLength() * 3 / 5)
        val ea = Elliptic.create(t0, t1, fsc)
        val ca = Circular.create(t0, t1, fsc)
        val ee = FileReader(path.resolve("Elliptic$i.json").toFile()).use { prettyGson.fromJson<EllipticJson>(it).elliptic() }
        println("mu(EA) = ${ea.isValidFor(fsc).value}")
        with(curveInput.contents) {
            cubicFsc(fsc){ stroke = Color.GREEN}
            fuzzyCurve(ea.fuzzyCurve) { stroke = Color.BLUE }
            fuzzyCurve(ee.fuzzyCurve) { stroke = Color.ORANGE}
            //fuzzyCurve(ca.fuzzyCurve) { stroke = Color.DARKGOLDENROD}
        }*/
        subscribe<CurveInput.CurveDoneEvent> {
            render(it.data)
        }
    }

    private fun render(data: Array<ParamPoint>): Unit {
        with(curveInput.contents) {
            val fsc = FscGenerator(3, 0.1).generate(Array.ofAll(data))
            cubicFsc(fsc) { stroke = Color.BLUE }
            val e = Elliptic.create(fsc.domain.begin, fsc.domain.end, fsc)
            fuzzyCurve(e.fuzzyCurve) { stroke = Color.GREEN}
            println(e.isValidFor(fsc))
            val result = Classifier4().classify(fsc)
            println(result.grades)
        }
    }
}
