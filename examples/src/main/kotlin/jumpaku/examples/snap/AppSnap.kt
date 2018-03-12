package jumpaku.examples.snap

import javafx.application.Application
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.fsc.classify.ClassifierOpen4
import jumpaku.fsc.classify.CurveClass
import jumpaku.fsc.classify.reference.Circular
import jumpaku.fsc.classify.reference.Elliptic
import jumpaku.fsc.classify.reference.Linear
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fsc.snap.*
import jumpaku.fsc.snap.point.PointSnapper
import jumpaku.fxcomponents.node.curveControl
import jumpaku.fxcomponents.node.onCurveDone
import jumpaku.fxcomponents.view.*
import tornadofx.*


fun main(vararg args: String) = Application.launch(AppSnap::class.java, *args)

class AppSnap : App(ViewSnap::class)

class ViewSnap : View() {

    private val baseGrid = BaseGrid(spacing = 50.0, magnification = 4, fuzziness = 10.0)

    private val pointSnapper = PointSnapper(baseGrid, -1, 1)

    override val root: Pane = pane {
        val w = 1280.0
        val h = 720.0
        val pane = pane {
            prefWidth = w
            prefHeight = h
        }
        curveControl {
            prefWidth = w
            prefHeight = h
            onCurveDone { e ->
                clear()
                pane.render(FscGenerator().generate(e.data))
            }
        }
    }

    private fun conicSection(fsc: BSpline, curveClass: CurveClass): ConicSection {
        require(curveClass.isConicSection) { "curveClass($curveClass) must be conic section" }
        return when {
            curveClass.isLinear -> Linear.ofBeginEnd(fsc).reference.conicSection
            curveClass.isCircular -> Circular.ofBeginEnd(fsc).reference.conicSection
            else -> Elliptic.ofBeginEnd(fsc).reference.conicSection
        }
    }

    private fun Pane.render(fsc: BSpline) {
        children.clear()
        cubicFsc(fsc) { stroke = Color.BLACK }
        val r4 = ClassifierOpen4().classify(fsc).curveClass
        if (r4.isConicSection) {
        }
    }
}

