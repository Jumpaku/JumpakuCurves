package jumpaku.examples.classify

import com.github.salomonbrys.kotson.fromJson
import io.vavr.collection.Array
import io.vavr.control.Try
import javafx.application.Application
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.bspline.BSplineJson
import jumpaku.core.json.prettyGson
import jumpaku.fsc.fragment.Fragment
import jumpaku.fsc.fragment.Fragmenter
import jumpaku.fxcomponents.view.CurveInput
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
            render(it.data)
        }
    }

    private fun render(data: Array<ParamPoint>) {
        if (data.size() <= 2) {
            return
        }
        with(curveInput.contents) {
            val path = Paths.get("./fsc/src/test/resources/jumpaku/fsc/fragment/")
            val dataFile = path.resolve("FragmenterTestFsc1.json").toFile()
            val fsc = prettyGson.fromJson<BSplineJson>(dataFile.readText()).bSpline()
            Try.run {
                val r = Fragmenter().fragment(fsc)
                r.fragments.forEach {
                    fuzzyCurve(fsc.restrict(it.interval)) {
                        stroke = if (it.type == Fragment.Type.IDENTIFICATION) Color.LIGHTGREEN else Color.ORANGERED
                    }
                }
                r.fragments.forEachIndexed { index, fragment ->
                    val file = path.resolve("FragmenterTestData1_${index}.json").toFile()
                    file.writeText(fsc.restrict(fragment.interval).toString())
                }
            }.onFailure {
                println("the following fsc causes a classification error")
                println(fsc)
            }
        }
    }
}
