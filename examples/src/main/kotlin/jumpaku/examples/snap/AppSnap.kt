package jumpaku.examples.snap

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.arclength.ReparametrizedCurve
import jumpaku.core.geom.Point
import jumpaku.core.geom.Vector
import jumpaku.core.transform.Rotate
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.fsc.identify.CurveClass
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fsc.identify.Open4Identifier
import jumpaku.fsc.identify.reference.CircularGenerator
import jumpaku.fsc.identify.reference.EllipticGenerator
import jumpaku.fsc.identify.reference.LinearGenerator
import jumpaku.fsc.identify.reference.ReferenceGenerator
import jumpaku.fsc.identify.reparametrize
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


fun main(vararg args: String) = Application.launch(AppSnap::class.java, *args)

class AppSnap : App(ViewSnap::class)

class ViewSnap : View() {

    val generator = FscGenerator(
            degree = 3,
            knotSpan = 0.1)

    val w = 1280.0
    val h = 720.0

    val baseGrid = Grid(
            spacing = 50.0,
            magnification = 2,
            origin = Point.xy(w/2, h/2),
            rotation = Rotate(Vector.K, 0.0),
            fuzziness = 20.0)

    val conicSectionSnapper = ConicSectionSnapper(
            PointSnapper(
                    baseGrid = baseGrid,
                    minResolution = -5,
                    maxResolution = 5),
            ConjugateCombinator())

    val classifier = Open4Identifier(nSamples = 99)

    fun conicSection(fsc: ReparametrizedCurve<BSpline>, curveClass: CurveClass): ConicSection = when {

        curveClass.isLinear -> LinearGenerator().generate(fsc).base
        curveClass.isCircular -> CircularGenerator(25).generateScattered(fsc).base
        curveClass.isElliptic -> EllipticGenerator(25).generateScattered(fsc).base
        else -> error("")
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
                    this.update(generator.generate(it.data))
                }
            }
        }
    }

    fun Group.update(fsc: BSpline) {
        grid(baseGrid, 0.0, 0.0, w, h) { stroke = Color.BLACK }

        cubicFsc(fsc) { stroke = Color.BLACK }

        val reparametrized = reparametrize(fsc)
        val classifyResult = classifier.identify(reparametrized)
        println(classifyResult.curveClass)
        if (classifyResult.curveClass.isFreeCurve) {
            cubicFsc(fsc.toCrisp()) { stroke = Color.RED }
            return
        }
        val cs = conicSection(reparametrized, classifyResult.curveClass)
        val snapped = conicSectionSnapper.snap(cs, classifyResult.curveClass) { candidate ->
            reparametrized.isPossible(ReferenceGenerator.reparametrize(candidate.snappedConicSection), 15)
        }

        fuzzyCurve(cs.toCrisp()) { stroke = Color.BLUE }
        fuzzyCurve(snapped.candidate.snappedConicSection) { stroke = Color.RED }

        conjugateBox(ConjugateBox.ofConicSection(snapped.candidate.snappedConicSection)) { stroke = Color.GREEN }
        snapped.candidate.featurePoints.forEach { snappedPoint(it.snapped.get()) { stroke = Color.GREEN; fill = Color.GREEN } }
    }
}

