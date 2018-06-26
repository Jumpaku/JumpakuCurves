package jumpaku.examples.classify

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.bspline.BSpline
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


fun main(vararg args: String) = Application.launch(AppClassify::class.java, *args)

class AppClassify : App(ViewClassify::class)

class ViewClassify : View() {

    val path = Paths.get("./fsc-test/src/test/resources/jumpaku/fsc/test/identify/reference")
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
        println(path.toFile().exists())
    }
    fun Group.update(fsc: BSpline){
        children.clear()
        val s = reparametrize(fsc, 65)
        //val a = EllipticGenerator().generate(s, t0 = s.originalCurve.domain.begin, t1 = s.originalCurve.domain.end)
        path.resolve("FscFC.json").toFile().apply { createNewFile() }.writeText(fsc.toString())
        //path.resolve("ReferenceElliptic.json").toFile().apply { createNewFile() }.writeText(a.toString())
        //val o = Open4Identifier(25, 15).identify(s)
        val p = Primitive7Identifier(25, 15).identify(s)
        println(p.curveClass == CurveClass.ClosedFreeCurve)
        cubicSpline(fsc) { stroke = Color.BLACK }
        fuzzyPoints(s.evaluateAll(15)) { stroke = Color.BLACK }
        //fuzzyCurve(a.reparametrized.toCrisp()) { stroke = Color.RED }
        //fuzzyPoints(a.reparametrized.evaluateAll(15)) { stroke = Color.RED }
        //fuzzyCurve(p.elliptic.reparametrized.toCrisp()) { stroke = Color.BLUE }
        //fuzzyPoints(p.elliptic.reparametrized.evaluateAll(15)) { stroke = Color.BLUE }
    }
}

