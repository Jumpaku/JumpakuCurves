package jumpaku.examples.snap

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.fsc.classify.ClassifierOpen4
import jumpaku.fsc.classify.CurveClass
import jumpaku.fsc.classify.reference.Circular
import jumpaku.fsc.classify.reference.Elliptic
import jumpaku.fsc.classify.reference.Linear
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.conicsection.ConicSectionSnapper
import jumpaku.fsc.snap.conicsection.ConjugateBox
import jumpaku.fsc.snap.conicsection.ConjugateCombinator
import jumpaku.fsc.snap.point.PointSnapper
import jumpaku.fxcomponents.nodes.*
import tornadofx.App
import tornadofx.View
import tornadofx.group
import tornadofx.pane
import java.io.File


fun main(vararg args: String) = Application.launch(AppSnap::class.java, *args)

class AppSnap : App(ViewSnap::class)

class ViewSnap : View() {

    val w = 1280.0

    val h = 720.0

    val baseGrid = Grid(
            spacing = 50.0,
            magnification = 2,
            origin = Point.xy(w/2, h/2),
            axis = Vector.K,
            radian = 0.0,
            fuzziness = 20.0)

    val conicSectionSnapper = ConicSectionSnapper(
            PointSnapper(
                    baseGrid = baseGrid,
                    minResolution = -5,
                    maxResolution = 5),
            ConjugateCombinator())

    val classifier = ClassifierOpen4(nSamples = 99)

    fun conicSection(fsc: BSpline, curveClass: CurveClass): ConicSection = when {
        curveClass.isLinear -> Linear.ofBeginEnd(fsc).reference.conicSection
        curveClass.isCircular -> Circular.ofBeginEnd(fsc).reference.conicSection
        curveClass.isElliptic -> Elliptic.ofBeginEnd(fsc, nSamples = 99).reference.conicSection
        else -> kotlin.error("")
    }

    override val root: Pane = pane {
        val group = group { }
        curveControl {
            prefWidth = w
            prefHeight = h
            onCurveDone {
                clear()
                with(group) {
                    children.clear()
                    this.update(FscGenerator { crisp, ts ->
                        val derivative1 = crisp.derivative
                        val derivative2 = derivative1.derivative
                        val velocityCoefficient = 0.008
                        val accelerationCoefficient = 0.006
                        ts.map {
                            val v = derivative1(it).length()
                            val a = derivative2(it).length()
                            velocityCoefficient * v + a * accelerationCoefficient + 1.0
                        }
                    }.generate(it.data))
                }
            }
        }
    }

    fun Group.update(fsc: BSpline) {
        grid(baseGrid, 0.0, 0.0, w, h) { stroke = Color.BLACK }

        cubicFsc(fsc) { stroke = Color.BLACK }

        val classifyResult = classifier.classify(fsc)
        println(classifyResult.curveClass)
        if (classifyResult.curveClass.isFreeCurve) {
            cubicFsc(fsc.toCrisp()) { stroke = Color.RED }
            return
        }
        val cs = conicSection(fsc, classifyResult.curveClass)
        val snapped = conicSectionSnapper.snap(cs, classifyResult.curveClass) { candidate ->
            candidate.snappedConicSection.isPossible(fsc, n = 15)
        }

        fuzzyCurve(cs.toCrisp()) { stroke = Color.BLUE }
        fuzzyCurve(snapped.candidate.snappedConicSection) { stroke = Color.RED }

        conjugateBox(ConjugateBox.ofConicSection(snapped.candidate.snappedConicSection)) { stroke = Color.GREEN }
        snapped.candidate.featurePoints.forEach { snappedPoint(it.snapped) { stroke = Color.GREEN; fill = Color.GREEN } }

        File("./Fsc.json").writeText(fsc.toString())
        File("./ClassifyResult.json").writeText(classifyResult.toString())
        File("./ConicSection.json").writeText(cs.toString())
        File("./ConicSectionSnapResult.json").writeText(snapped.toString())
    }
}

