package jumpaku.examples.snap

import io.vavr.collection.Stream
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.fsc.classify.ClassifierOpen4
import jumpaku.fsc.classify.CurveClass
import jumpaku.fsc.classify.reference.Circular
import jumpaku.fsc.classify.reference.Elliptic
import jumpaku.fsc.classify.reference.Linear
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fsc.snap.BaseGrid
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.GridPoint
import jumpaku.fsc.snap.conicsection.ConicSectionSnapper
import jumpaku.fsc.snap.conicsection.ConjugateBox
import jumpaku.fsc.snap.conicsection.ConjugateCombinator
import jumpaku.fsc.snap.point.PointSnapResult
import jumpaku.fsc.snap.point.PointSnapper
import jumpaku.fxcomponents.node.curveControl
import jumpaku.fxcomponents.node.onCurveDone
import jumpaku.fxcomponents.view.cubicFsc
import jumpaku.fxcomponents.view.fuzzyCurve
import jumpaku.fxcomponents.view.polyline
import tornadofx.*


fun main(vararg args: String) = Application.launch(AppSnap::class.java, *args)

class AppSnap : App(ViewSnap::class)

class ViewSnap : View() {

    val w = 1280.0

    val h = 720.0

    val conicSectionSnapper = ConicSectionSnapper(
            PointSnapper(
                    BaseGrid(
                            spacing = 50.0,
                            magnification = 2,
                            origin = Point.xy(w/2, h/2),
                            axis = Vector.K,
                            radian = 0.0,
                            fuzziness = 10.0),
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
        val group = group {  }
        curveControl {
            prefWidth = w
            prefHeight = h
            onCurveDone {
                clear()
                with(group) {
                    children.clear()
                    this.update(FscGenerator().generate(it.data))
                }
            }
        }
    }

    fun Group.snappedPoint(pointSnapResult: PointSnapResult){
        val a = pointSnapResult.grid.localToWorld
        val g00 = GridPoint(pointSnapResult.gridPoint.x - 1, pointSnapResult.gridPoint.y - 1, pointSnapResult.gridPoint.z).toWorldPoint(a)
        val g01 = GridPoint(pointSnapResult.gridPoint.x + 0, pointSnapResult.gridPoint.y - 1, pointSnapResult.gridPoint.z).toWorldPoint(a)
        val g02 = GridPoint(pointSnapResult.gridPoint.x + 1, pointSnapResult.gridPoint.y - 1, pointSnapResult.gridPoint.z).toWorldPoint(a)
        val g10 = GridPoint(pointSnapResult.gridPoint.x - 1, pointSnapResult.gridPoint.y + 0, pointSnapResult.gridPoint.z).toWorldPoint(a)
        val g11 = GridPoint(pointSnapResult.gridPoint.x + 0, pointSnapResult.gridPoint.y + 0, pointSnapResult.gridPoint.z).toWorldPoint(a)
        val g12 = GridPoint(pointSnapResult.gridPoint.x + 1, pointSnapResult.gridPoint.y + 0, pointSnapResult.gridPoint.z).toWorldPoint(a)
        val g20 = GridPoint(pointSnapResult.gridPoint.x - 1, pointSnapResult.gridPoint.y + 1, pointSnapResult.gridPoint.z).toWorldPoint(a)
        val g21 = GridPoint(pointSnapResult.gridPoint.x + 0, pointSnapResult.gridPoint.y + 1, pointSnapResult.gridPoint.z).toWorldPoint(a)
        val g22 = GridPoint(pointSnapResult.gridPoint.x + 1, pointSnapResult.gridPoint.y + 1, pointSnapResult.gridPoint.z).toWorldPoint(a)
        line(g00.x, g00.y, g02.x, g02.y) { stroke = Color.GREEN }
        line(g10.x, g10.y, g12.x, g12.y) { stroke = Color.GREEN }
        line(g20.x, g20.y, g22.x, g22.y) { stroke = Color.GREEN }
        line(g00.x, g00.y, g20.x, g20.y) { stroke = Color.GREEN }
        line(g01.x, g01.y, g21.x, g21.y) { stroke = Color.GREEN }
        line(g02.x, g02.y, g22.x, g22.y) { stroke = Color.GREEN }
        circle(g11.x, g11.y, 3.0) { stroke = Color.GREEN; fill = Color.GREEN }
    }

    fun Group.conjugateBox(conjugateBox: ConjugateBox) {
        polyline(Polyline(conjugateBox.bottomLeft, conjugateBox.topLeft, conjugateBox.topRight, conjugateBox.bottomRight, conjugateBox.bottomLeft)) {
            stroke = Color.GREEN
        }
        polyline(Polyline(conjugateBox.left, conjugateBox.top, conjugateBox.right, conjugateBox.bottom, conjugateBox.left)) { stroke = Color.GREEN }
    }

    fun Group.grid(grid: Grid) {
        Stream.concat(Stream.iterate(0) { it + 1 }.map { grid.origin.x + grid.spacing * it }.takeWhile { it in 0.0..w },
                Stream.iterate(0) { it + 1 }.map { grid.origin.x - grid.spacing * it }.takeWhile { it in 0.0..w })
                .forEach { line(it, 0.0, it, h) { stroke = Color.BLACK } }
        Stream.concat(Stream.iterate(0) { it + 1 }.map { grid.origin.y + grid.spacing * it }.takeWhile { it in 0.0..h },
                Stream.iterate(0) { it + 1 }.map { grid.origin.y - grid.spacing * it }.takeWhile { it in 0.0..h })
                .forEach { line(0.0, it, w, it) { stroke = Color.BLACK } }
    }

    fun Group.update(fsc: BSpline) {

        grid(conicSectionSnapper.pointSnapper.baseGrid)

        cubicFsc(fsc) { stroke = Color.BLACK }

        val curveClass = classifier.classify(fsc).curveClass
        println(curveClass)
        if (curveClass.isFreeCurve) {
            cubicFsc(fsc.toCrisp()) { stroke = Color.RED }
            return
        }
        val cs = conicSection(fsc, curveClass)
        val snapped = conicSectionSnapper.snap(cs, curveClass) { candidate ->
            candidate.snappedConicSection.isPossible(fsc, n = 15)
        }

        fuzzyCurve(cs.toCrisp()) { stroke = Color.BLUE }
        fuzzyCurve(snapped.candidate.snappedConicSection) { stroke = Color.RED }

        conjugateBox(ConjugateBox.ofConicSection(snapped.candidate.snappedConicSection))
        snapped.candidate.featurePoints.forEach { snappedPoint(it.snapped) }

    }
}

