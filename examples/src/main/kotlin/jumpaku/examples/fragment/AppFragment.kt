package jumpaku.examples.fragment

import io.vavr.collection.Array
import io.vavr.control.Try
import javafx.application.Application
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.ParamPoint
import jumpaku.fsc.fragment.Fragment
import jumpaku.fsc.fragment.Fragmenter
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fxcomponents.view.CurveInput
import jumpaku.fxcomponents.view.fuzzyCurve
import tornadofx.App
import tornadofx.Scope
import tornadofx.View


fun main(vararg args: String) = Application.launch(AppFragment::class.java, *args)

class AppFragment : App(ViewFragment::class)

class ViewFragment : View() {

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
            val fsc = FscGenerator(3, 0.1).generate(data)//prettyGson.fromJson<BSplineJson>(File("/Users/jumpaku/Documents/fsc.json").readText()).bSpline()//
            //cubicFsc(fsc) { stroke = Color.BLUE }
            Try.run {
                val r = Fragmenter().fragment(fsc)
                r.fragments.forEach {
                    fuzzyCurve(fsc.restrict(it.interval)) {
                        stroke = if (it.type == Fragment.Type.IDENTIFICATION) Color.LIGHTGREEN else Color.ORANGERED
                    }
                }
            }.onFailure {
                println("the following fsc caused a classification error")
                println(fsc)
            }
        }
    }
}
