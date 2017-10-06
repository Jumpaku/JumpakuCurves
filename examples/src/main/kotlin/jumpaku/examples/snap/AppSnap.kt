package jumpaku.examples.snap

import io.vavr.collection.Stream
import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.classify.ClassifierOpen4
import jumpaku.fsc.classify.CurveClass
import jumpaku.fsc.classify.reference.Circular
import jumpaku.fsc.classify.reference.Elliptic
import jumpaku.fsc.classify.reference.Linear
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fsc.snap.*
import jumpaku.fxcomponents.view.*
import tornadofx.App
import tornadofx.Scope
import tornadofx.View


fun main(vararg args: String) = Application.launch(AppClassify::class.java, *args)

class AppClassify : App(ViewClassify::class)

class ViewClassify : View() {

    override val scope: Scope = Scope()

    override val root: Pane

    private val curveInput: CurveInput

    private val baseGrid = BaseGrid(
            baseGridSpacing = 100.0,
            magnification = 4)

    private val pointSnapper = PointSnapper(baseGrid, -2, 5)

    init {
        curveInput = CurveInput(scope = scope)
        root = curveInput.root
        subscribe<CurveInput.CurveDoneEvent> {
            if (it.data.size() > 2) {
                val fsc = FscGenerator(3, 0.1).generate(it.data)
                with(curveInput.contents) {
                    children.clear()
                    render(fsc)
                }
            }
        }
    }

    private fun Parent.render(fsc: BSpline) {
        cubicFsc(fsc) { stroke = Color.RED }
        val c4 = ClassifierOpen4()
        val r4 = c4.classify(fsc)
        fun snapping(snapResult: Stream<ConicSectionSnapResult>) {
            val (fs, ss, _, curve) = snapResult.maxBy { r -> r.snappedConicSection.isPossible(fsc, 10).value }.get()
            fs.map { fuzzyPoint(it.point.copy(r = 5.0)) { fill = Color.GREEN } }
            ss.map { fuzzyPoint(it.snappedPoint.copy(r = 5.0)) { fill = Color.ORANGE } }
            ss.flatMap { it.grid }.maxBy { g -> g.resolution }.forEach { grid(it, root.width, root.height) { stroke = Color.BLACK } }
            fuzzyCurve(curve) { stroke = Color.BLUE }
        }
        when(r4.curveClass) {
            CurveClass.LineSegment -> {
                snapping(LinearSnapper(pointSnapper).snap(Linear.ofBeginEnd(fsc).conicSection))
            }
            CurveClass.CircularArc -> {
                snapping(CircularSnapper(pointSnapper).snap(Circular.ofBeginEnd(fsc).conicSection))
            }
            CurveClass.EllipticArc -> {
                snapping(EllipticSnapper(pointSnapper).snap(Elliptic.ofBeginEnd(fsc).conicSection))
            }
            else -> {}//fsc.toBeziers().map { ConicSection.ofQuadraticBezier(it) }
        }
    }
}
