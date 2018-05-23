package jumpaku.examples.classify

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.geom.divide
import jumpaku.fsc.classify.ClassifierPrimitive7
import jumpaku.fsc.classify.reference.CircularGenerator
import jumpaku.fsc.classify.reference.EllipticGenerator
import jumpaku.fsc.classify.reference.LinearGenerator
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fxcomponents.nodes.cubicFsc
import jumpaku.fxcomponents.nodes.curveControl
import jumpaku.fxcomponents.nodes.fuzzyCurve
import jumpaku.fxcomponents.nodes.onCurveDone
import tornadofx.App
import tornadofx.View
import tornadofx.group
import tornadofx.pane


fun main(vararg args: String) = Application.launch(AppClassify::class.java, *args)

class AppClassify : App(ViewClassify::class)

class ViewClassify : View() {

    override val root: Pane = pane {
        val group = group {  }
        curveControl {
            prefWidth = 1280.0
            prefHeight = 720.0
            onCurveDone { e ->
                clear()
                group.update(FscGenerator().generate(e.data))
            }
        }
    }

    fun Group.update(fsc: BSpline){
        children.clear()
        cubicFsc(fsc) { stroke = Color.BLACK; strokeWidth = 1.0 }
        listOf(
                LinearGenerator(25).generate(fsc),
                CircularGenerator(25).generateScattered(fsc),
                EllipticGenerator(25).generateScattered(fsc)
        ).forEachIndexed { i, r ->
            fuzzyCurve(r) { stroke = Color.hsb(i * 120.0, 1.0, 1.0); strokeWidth = 1.0 }
        }

        val r7 = ClassifierPrimitive7(nSamples = 25, nFmps = 15).classify(fsc)
        println(r7.grades)
    }
}

