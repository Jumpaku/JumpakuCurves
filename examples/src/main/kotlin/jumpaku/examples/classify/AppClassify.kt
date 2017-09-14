package jumpaku.examples.classify

import io.vavr.collection.Array
import io.vavr.control.Try
import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.classify.ClassifierOpen4
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fsc.classify.ClassifierPrimitive7
import jumpaku.fsc.classify.reference.Circular
import jumpaku.fsc.classify.reference.Elliptic
import jumpaku.fsc.classify.reference.Linear
import jumpaku.fxcomponents.view.CurveInput
import jumpaku.fxcomponents.view.cubicFsc
import jumpaku.fxcomponents.view.fuzzyCurve
import tornadofx.App
import tornadofx.Scope
import tornadofx.View
import java.nio.file.Paths


fun main(vararg args: String) = Application.launch(AppClassify::class.java, *args)

class AppClassify : App(ViewClassify::class)

class ViewClassify : View() {

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
        fuzzyCurve(Linear.of(fsc).reference) { stroke = Color.GREEN }
        fuzzyCurve(Circular.of(fsc).reference) { stroke = Color.BLUE }
        fuzzyCurve(Elliptic.of(fsc).reference) { stroke = Color.ORANGE }
        Try.run {
            val r7 = ClassifierPrimitive7().classify(fsc)
            println("${r7.curveClass} : ${r7.grade}")
            val r4 = ClassifierOpen4().classify(fsc)
            println("${r4.curveClass} : ${r4.grade}")
            //r7.grades.forEach { println("\t$it") }

            path.resolve("Fsc$i.json").toFile().writeText(fsc.toString())
            path.resolve("Primitive7Result$i.json").toFile().writeText(ClassifierPrimitive7().classify(fsc).toString())
            path.resolve("Open4Result$i.json").toFile().writeText(ClassifierOpen4().classify(fsc).toString())
            ++i
        }.onFailure {
            println("the following fsc caused a classification error")
            println(fsc)
        }
    }
}
