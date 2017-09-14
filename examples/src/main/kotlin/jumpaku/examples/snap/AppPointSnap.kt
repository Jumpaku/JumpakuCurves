package jumpaku.examples.snap

import io.vavr.collection.Stream
import io.vavr.control.Try
import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.affine.Point
import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.classify.ClassifierOpen4
import jumpaku.fsc.classify.ClassifierPrimitive7
import jumpaku.fsc.classify.reference.Circular
import jumpaku.fsc.classify.reference.Elliptic
import jumpaku.fsc.classify.reference.Linear
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fsc.snap.BaseGrid
import jumpaku.fsc.snap.PointSnapper
import jumpaku.fxcomponents.view.CurveInput
import jumpaku.fxcomponents.view.cubicFsc
import jumpaku.fxcomponents.view.fuzzyCurve
import tornadofx.App
import tornadofx.Scope
import tornadofx.View
import tornadofx.line
import java.nio.file.Paths


fun main(vararg args: String) = Application.launch(AppPointSnap::class.java, *args)

class AppPointSnap : App(ViewPointSnap::class)

class ViewPointSnap : View() {

    override val scope: Scope = Scope()

    override val root: Pane

    private val curveInput: CurveInput

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

    var i = 0
    val path = Paths.get("./")

    private fun Parent.render(fsc: BSpline) {
        cubicFsc(fsc) { stroke = Color.RED }
        val baseGrid = BaseGrid(64.0, origin = Point.xy(curveInput.width/2, curveInput.height/2))
        Stream.from(0).map { baseGrid.origin.x + it*baseGrid.gridSpacing } .takeWhile { it <= curveInput.width }
                .appendAll(Stream.from(-1).map { baseGrid.origin.x - it*baseGrid.gridSpacing } .takeWhile { it >= 0 })
                .forEach { line(it, 0, it, curveInput.height) { stroke = Color.BLACK } }
        Stream.from(0).map { baseGrid.origin.y + it*baseGrid.gridSpacing } .takeWhile { it <= curveInput.height }
                .appendAll(Stream.from(-1).map { baseGrid.origin.y - it*baseGrid.gridSpacing } .takeWhile { it >= 0 })
                .forEach { line(0, it, curveInput.width, it) { stroke = Color.BLACK } }

        Try.run {
            val s = PointSnapper(baseGrid)

        }.onFailure {
            println("the following fsc caused a classification error")
            println(fsc)
        }
    }
}
