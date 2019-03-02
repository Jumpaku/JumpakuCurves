package jumpaku.examples.identify

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.identify.primitive.CurveClass
import jumpaku.curves.fsc.identify.primitive.Open4Identifier
import jumpaku.curves.fsc.identify.primitive.Primitive7Identifier
import jumpaku.curves.fsc.identify.primitive.reparametrize
import jumpaku.fxcomponents.nodes.*
import tornadofx.App
import tornadofx.View
import tornadofx.group
import tornadofx.pane


fun main(vararg args: String) = Application.launch(AppIdentify::class.java, *args)

class AppIdentify : App(ViewIdentify::class)

class ViewIdentify : View() {

    override val root: Pane = pane {
        val group = group {}
        curveControl {
            prefWidth = 1280.0
            prefHeight = 720.0
            onCurveDone { e ->
                clear()
                group.update(Generator().generate(e.data))
            }
        }
    }

    init {
        /*val s = Paths.get("./fsc-test/src/test/resources/jumpaku/fsc/test/identify/reference/FscFO0.json")
                .parseJson().tryFlatMap { BSpline.fromJson(it) }.orThrow()
        val reparametrized = reparametrize(s, 65)
        val time = 1.0e-9* measureNanoTime {
            repeat(1000) {
                Open4Identifier(25, 15).identify(reparametrized)
            }
        }
        println(time)*/
    }

    fun Group.update(fsc: BSpline){
        children.clear()
        val s = reparametrize(fsc)
        val p = Primitive7Identifier(25, 15).identify(s)
        val o = Open4Identifier(25, 15).identify(s)
        //println(p.curveClass == CurveClass.ClosedFreeCurve)
        fuzzyCurve(fsc) { stroke = Color.BLACK }
        fuzzyCurve(when(o.curveClass) {
            CurveClass.LineSegment -> o.linear
            CurveClass.CircularArc -> o.circular
            CurveClass.EllipticArc -> o.elliptic
            else -> fsc
        }){ stroke = Color.RED }
        //fuzzyPoints(s.evaluateAll(15)) { stroke = Color.BLACK }
    }
}

