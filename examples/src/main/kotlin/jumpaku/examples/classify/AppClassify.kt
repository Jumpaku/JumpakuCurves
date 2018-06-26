package jumpaku.examples.classify

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fsc.identify.CurveClass
import jumpaku.fsc.identify.Open4Identifier
import jumpaku.fsc.identify.Primitive7Identifier
import jumpaku.fsc.identify.reference.CircularGenerator
import jumpaku.fsc.identify.reference.EllipticGenerator
import jumpaku.fsc.identify.reference.LinearGenerator
import jumpaku.fsc.identify.reparametrize
import jumpaku.fxcomponents.nodes.*
import tornadofx.App
import tornadofx.View
import tornadofx.group
import tornadofx.pane
import java.nio.file.Paths
import kotlin.system.measureNanoTime


fun main(vararg args: String) = Application.launch(AppClassify::class.java, *args)

class AppClassify : App(ViewClassify::class)

class ViewClassify : View() {

    override val root: Pane = pane {
        val group = group {}
        curveControl {
            prefWidth = 1280.0
            prefHeight = 720.0
            onCurveDone { e ->
                clear()
                group.update(FscGenerator().generate(e.data))
            }
        }
    }

    init {
        val s = Paths.get("./fsc-test/src/test/resources/jumpaku/fsc/test/identify/reference/FscFO0.json")
                .parseJson().flatMap { BSpline.fromJson(it) }.get()
        val reparametrized = reparametrize(s, 65)
        val time = 1.0e-9* measureNanoTime {
            repeat(1000) {
                Open4Identifier(25, 15).identify(reparametrized)
            }
        }
        println(time)
    }

    fun Group.update(fsc: BSpline){
        children.clear()
        val s = reparametrize(fsc, 65)
        val p = Primitive7Identifier(25, 15).identify(s)
        println(p.curveClass == CurveClass.ClosedFreeCurve)
        cubicSpline(fsc) { stroke = Color.BLACK }
        fuzzyPoints(s.evaluateAll(15)) { stroke = Color.BLACK }
    }
}

