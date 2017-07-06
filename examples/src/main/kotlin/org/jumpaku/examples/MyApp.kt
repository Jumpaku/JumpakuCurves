package org.jumpaku.examples

import com.github.salomonbrys.kotson.fromJson
import io.vavr.collection.Array
import javafx.application.Application
import javafx.scene.paint.Color
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.arclength.ArcLengthAdapter
import org.jumpaku.core.curve.bspline.BSplineJson
import org.jumpaku.core.fsci.FscGeneration
import org.jumpaku.core.fsci.reference.Circular
import org.jumpaku.core.fsci.reference.Elliptic
import org.jumpaku.core.fsci.reference.Linear
import org.jumpaku.core.json.prettyGson
import org.jumpaku.fxcomponents.view.*
import tornadofx.App
import tornadofx.Scope
import tornadofx.View
import java.io.FileReader
import java.io.FileWriter


fun main(args: kotlin.Array<String>): Unit = Application.launch(MyApp::class.java, *args)

class MyApp: App(TestView::class)

class TestView : View(){

    override val scope: Scope = Scope()

    val curveInput = CurveInput(scope = scope)

    override val root = curveInput.root

    init {
        val i = 9
        val fsc = FileReader("./Fsc$i.json").use { prettyGson.fromJson<BSplineJson>(it).bSpline() }
        val arcLength = fsc.toArcLengthCurve()
        val t0 = arcLength.toOriginalParam(arcLength.arcLength()/5)
        val t1 = arcLength.toOriginalParam(arcLength.arcLength()*3/5)
        val l = Linear.create(t0, t1, fsc)
        val c = Circular.create(t0, t1, fsc)
        val e = Elliptic.create(t0, t1, fsc)
        FileWriter("./Linear$i.json").use { prettyGson.toJson(l.json(), it) }
        FileWriter("./Circular$i.json").use { prettyGson.toJson(c.json(), it) }
        FileWriter("./Elliptic$i.json").use { prettyGson.toJson(e.json(), it) }

        val ml = Linear.create(t0, t1, fsc).isValidFor(fsc)
        val mc = Circular.create(t0, t1, fsc).isValidFor(fsc)
        val me = Elliptic.create(t0, t1, fsc).isValidFor(fsc)
        println("linear : $ml")
        println("Circular : $mc")
        println("Elliptic : $me")
        FileWriter("./LinearGrade$i.json").use { prettyGson.toJson(ml, it) }
        FileWriter("./CircularGrade$i.json").use { prettyGson.toJson(mc, it) }
        FileWriter("./EllipticGrade$i.json").use { prettyGson.toJson(me, it) }

        with(curveInput.contents) {
            cubicFsc(fsc) { stroke = Color.RED }
            fuzzyCurve(l.fuzzyCurve) { stroke = Color.BLUE }
            fuzzyCurve(c.fuzzyCurve) { stroke = Color.GREEN }
            fuzzyCurve(e.fuzzyCurve) { stroke = Color.ORANGE }
        }
        subscribe<CurveInput.CurveDoneEvent> {
            //render(it.data)
        }
    }

    private fun render(data: Array<ParamPoint>): Unit {
        with(curveInput.contents) {
            FscGeneration(3, 0.1).generate(Array.ofAll(data))
                    .run {
                        cubicFsc(this) { stroke = Color.RED }
                        FileWriter("./Fsc.json").use { prettyGson.toJson(this@run.json(), it) }
                    }
        }
    }
}
