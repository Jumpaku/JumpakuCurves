package jumpaku.examples.generate

import com.github.salomonbrys.kotson.jsonArray
import io.vavr.control.Try
import javafx.application.Application
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fxcomponents.view.CurveInputView
import jumpaku.fxcomponents.view.cubicFsc
import tornadofx.App
import tornadofx.Scope
import tornadofx.View
import java.nio.file.Paths



fun main(vararg args: String) = Application.launch(AppFscGenerate::class.java, *args)

class AppFscGenerate : App(ViewFscGenerate::class)

class ViewFscGenerate : View() {

    override val scope: Scope = Scope()

    override val root: Pane

    private val curveInputView: CurveInputView


    var i = 0
    val path = Paths.get("./")

    init {
        curveInputView = CurveInputView(scope = scope)
        root = curveInputView.root
        subscribe<CurveInputView.CurveDoneEvent> {
            val data = it.data
            with(curveInputView.contents) {
                if (data.size() > 2) {
                    children.clear()
                    Try.run {
                        val fsc = FscGenerator(degree = 3, knotSpan = 0.1, generateFuzziness = { crisp, ts ->
                            val derivative1 = crisp.derivative
                            val derivative2 = derivative1.derivative
                            val velocityCoefficient = 0.004
                            val accelerationCoefficient = 0.003
                            ts.map {
                                val v = derivative1(it).length()
                                val a = derivative2(it).length()
                                velocityCoefficient * v + a * accelerationCoefficient + 1.0
                            }
                        }).generate(data)
                        cubicFsc(fsc) { stroke = Color.RED }

                        path.resolve("Data$i.json").toFile().writeText(jsonArray(data.map { it.toJson() }).toString())
                        path.resolve("Fsc$i.json").toFile().writeText(fsc.toString())
                        ++i
                    }.onFailure {
                        println("the following data caused a fsc generation error")
                        println(data)
                    }
                }
            }
        }
    }

}
