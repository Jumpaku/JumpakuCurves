package jumpaku.examples.snap

import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.fsc.classify.ClassifierOpen4
import jumpaku.fsc.classify.CurveClass
import jumpaku.fsc.classify.reference.Circular
import jumpaku.fsc.classify.reference.Elliptic
import jumpaku.fsc.classify.reference.Linear
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fsc.snap.*
import jumpaku.fsc.snap.conicsection.*
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

    val conicSectionSnapper = ConicSectionSnapper(pointSnapper)

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
            curveClass.isLinear -> Linear.ofBeginEnd(fsc).conicSection
            curveClass.isCircular -> Circular.ofBeginEnd(fsc).conicSection
            else -> Elliptic.ofBeginEnd(fsc).conicSection
        }
    }

    private fun Pane.render(fsc: BSpline) {
        children.clear()
        cubicFsc(fsc) { stroke = Color.BLACK }
        val r4 = ClassifierOpen4().classify(fsc).curveClass
        if (r4.isConicSection) {
            val cs = conicSection(fsc, r4)
            val arcLength = fsc.reparametrizeArcLength()
            val (snapped, _, candidates) = conicSectionSnapper.snap(cs, r4) { it.conicSection.isPossible(arcLength, 20) }
            println(candidates.map { it.features.toArray().map { it._1 } })
            fuzzyCurve(snapped.conicSection.toCrisp()) { strokeWidth = 5.0; stroke = Color.RED }
            println(snapped.features.toArray().map { it._1 })
            snapped.features.forEach { (_, result) ->
                val (grid, _, _p, _) = result
                val p = _p.copy(r = 5.0)
                val s = grid.spacing
                group {
                    line(startX = p.x - s, startY = p.y - s, endX = p.x + s, endY = p.y - s) { stroke = Color.ORANGE }
                    line(startX = p.x - s, startY = p.y, endX = p.x + s, endY = p.y) { stroke = Color.ORANGE }
                    line(startX = p.x - s, startY = p.y + s, endX = p.x + s, endY = p.y + s) { stroke = Color.ORANGE }
                    line(startX = p.x - s, startY = p.y - s, endX = p.x - s, endY = p.y + s) { stroke = Color.ORANGE }
                    line(startX = p.x, startY = p.y - s, endX = p.x, endY = p.y + s) { stroke = Color.ORANGE }
                    line(startX = p.x + s, startY = p.y - s, endX = p.x + s, endY = p.y + s) { stroke = Color.ORANGE }
                }
                group {
                    circle(p.x, p.y, p.r) {
                        fill = Color.gray(0.0, 0.0)
                        stroke = Color.RED
                    }
                }
            }
        }

    }
}

