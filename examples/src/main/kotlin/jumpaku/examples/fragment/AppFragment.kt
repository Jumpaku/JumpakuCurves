package jumpaku.examples.fragment

import javafx.application.Application
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.fsc.fragment.Fragment
import jumpaku.fsc.fragment.Fragmenter
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fxcomponents.nodes.curveControl
import jumpaku.fxcomponents.nodes.onCurveDone
import jumpaku.fxcomponents.nodes.fuzzyCurve
import tornadofx.*


fun main(vararg args: String) = Application.launch(AppFragment::class.java, *args)

class AppFragment : App(ViewFragment::class)

class ViewFragment : View() {

    val generator = FscGenerator(3, 0.1)

    val fragmenter = Fragmenter()

    override val root: Pane = pane {
        val group = group { }
        curveControl {
            prefWidth = 640.0
            prefHeight = 480.0
            onCurveDone {
                clear()
                with(group) {
                    children.clear()
                    val fsc = generator.generate(it.data)
                    val r = fragmenter.fragment(fsc)
                    r.fragments.forEach {
                        fuzzyCurve(fsc.restrict(it.interval)) {
                            stroke = if (it.type == Fragment.Type.IDENTIFICATION) Color.LIGHTGREEN else Color.ORANGERED
                        }
                    }
                }
            }
        }
    }
}
