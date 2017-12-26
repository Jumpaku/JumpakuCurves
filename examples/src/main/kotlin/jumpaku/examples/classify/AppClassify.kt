package jumpaku.examples.classify

import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.classify.ClassifierOpen4
import jumpaku.fsc.classify.ClassifierPrimitive7
import jumpaku.fsc.classify.reference.Circular
import jumpaku.fsc.classify.reference.Elliptic
import jumpaku.fsc.classify.reference.Linear
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fxcomponents.view.CurveInputView
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

    private val curveInputView: CurveInputView

    init {
        curveInputView = CurveInputView(600.0, 480.0, scope = scope)
        root = curveInputView.root
        subscribe<CurveInputView.CurveDoneEvent> {
            if (it.data.size() > 2) {
                val fsc = FscGenerator(3, 0.1).generate(it.data)
                with(curveInputView.contents) {
                    children.clear()
                    render(fsc)
                }
            }
        }
    }

    private fun Parent.render(fsc: BSpline) {
        val nSample = 25
        val nFmps = 15
        cubicFsc(fsc) { stroke = Color.RED }
        fuzzyCurve(Linear.of(fsc).reference) { stroke = Color.GREEN }
        fuzzyCurve(Circular.of(fsc, nSample).reference) { stroke = Color.BLUE }
        fuzzyCurve(Elliptic.of(fsc, nSample).reference) { stroke = Color.ORANGE }
        val r7 = ClassifierPrimitive7(nSample, nFmps).classify(fsc)
        println("${r7.curveClass} : ${r7.grade}")
        val r4 = ClassifierOpen4(nSample, nFmps).classify(fsc)
        println("${r4.curveClass} : ${r4.grade}")
    }
}
