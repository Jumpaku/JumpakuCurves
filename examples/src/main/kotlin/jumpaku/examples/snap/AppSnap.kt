package jumpaku.examples.snap

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.geom.Point
import jumpaku.core.geom.Vector
import jumpaku.core.transform.Rotate
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fsc.identify.CurveClass
import jumpaku.fsc.identify.Open4Identifier
import jumpaku.fsc.identify.reference.reparametrize
import jumpaku.fsc.identify.reparametrize
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.conicsection.ConicSectionSnapper
import jumpaku.fsc.snap.conicsection.ConjugateBox
import jumpaku.fsc.snap.conicsection.ConjugateCombinator
import jumpaku.fsc.snap.point.MFGS
import jumpaku.fxcomponents.nodes.*
import tornadofx.*


fun main(vararg args: String) = Application.launch(AppSnap::class.java, *args)

class AppSnap : App(ViewSnap::class)

class ViewSnap : View() {

    val w = 1280.0
    val h = 720.0

    val generator = FscGenerator(
            degree = 3,
            knotSpan = 0.1)

    val baseGrid = Grid(
            spacing = 64.0,
            magnification = 2,
            origin = Point.xy(w/2, h/2),
            rotation = Rotate(Vector.K, 0.0),
            fuzziness = 16.0)

    val conicSectionSnapper = ConicSectionSnapper(
            MFGS(
                    baseGrid = baseGrid,
                    minResolution = -5,
                    maxResolution = 5),
            ConjugateCombinator())

    val identifier = Open4Identifier(nSamples = 25)

    override val root: Pane = pane {
        val group = group {}
        curveControl {
            prefWidth = w
            prefHeight = h
            onCurveDone {
                clear()
                with(group) {
                    children.clear()
                    update(generator.generate(it.data))
                }
            }
        }
    }

    fun Group.update(fsc: BSpline) {
        grid(baseGrid, 0.0, 0.0, w, h) { stroke = Color.BLACK }
        cubicFsc(fsc) { stroke = Color.BLACK }

        val identifyResult = identifier.identify(reparametrize(fsc))
        println(identifyResult.curveClass)
        if (identifyResult.curveClass.isFreeCurve) {
            cubicFsc(fsc.toCrisp()) { stroke = Color.RED }
            return
        }
        val cs: ConicSection = identifyResult.curveClass.let {
            when(it) {
                CurveClass.LineSegment -> identifyResult.linear.base
                CurveClass.CircularArc -> identifyResult.circular.base
                CurveClass.EllipticArc -> identifyResult.elliptic.base
                else -> kotlin.error("")
            }
        }
        val snapped = conicSectionSnapper.snap(cs, identifyResult.curveClass) { candidate ->
            reparametrize(cs).isPossible(reparametrize(candidate.snappedConicSection), 15)
        }

        fuzzyCurve(cs) { stroke = Color.BLUE }
        fuzzyCurve(snapped.candidate.snappedConicSection) { stroke = Color.RED }

        conjugateBox(ConjugateBox.ofConicSection(snapped.candidate.snappedConicSection)) { stroke = Color.GREEN }
        snapped.candidate.featurePoints.forEach { (c, s) ->
                s.onEmpty { circle(c.x, c.y, 3.0) { stroke = Color.GREEN; fill = Color.GREEN } }
                .forEach { snappedPoint(it) { stroke = Color.GREEN; fill = Color.GREEN } }
        }
    }
}

