package jumpaku.examples.classify

import io.vavr.collection.Array
import io.vavr.control.Try
import javafx.application.Application
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.ParamPoint
import jumpaku.core.json.parseToJson
import jumpaku.fsc.fragment.Fragment
import jumpaku.fsc.fragment.Fragmenter
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fsc.identify.classify.ClassifierOpen4
import jumpaku.fsc.identify.classify.ClassifierPrimitive7
import jumpaku.fsc.identify.classify.classifyResult
import jumpaku.fsc.identify.reference.Circular
import jumpaku.fsc.identify.reference.Elliptic
import jumpaku.fsc.identify.reference.Linear
import jumpaku.fxcomponents.view.CurveInput
import jumpaku.fxcomponents.view.cubicFsc
import jumpaku.fxcomponents.view.fuzzyCurve
import tornadofx.App
import tornadofx.Scope
import tornadofx.View


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
            render(it.data)
        }
    }

    private fun render(data: Array<ParamPoint>) {
        if (data.size() <= 2) {
            return
        }
        with(curveInput.contents) {
            children.clear()
            val fsc = FscGenerator(3, 0.1).generate(data)
            cubicFsc(fsc) { stroke = Color.RED }
            fuzzyCurve(Linear.of(fsc).reference) { stroke = Color.GREEN }
            fuzzyCurve(Circular.of(fsc).reference) { stroke = Color.BLUE }
            fuzzyCurve(Elliptic.of(fsc).reference) { stroke = Color.ORANGE }
            Try.run {
                val result = ClassifierPrimitive7().classify(fsc)
                println(result)
                println("${result.curveClass} : ${result.grade}")
                result.grades.forEach { println("\t$it") }
            }.onFailure {
                println("the following fsc caused a classification error")
                println(fsc)
            }
        }
    }
}
