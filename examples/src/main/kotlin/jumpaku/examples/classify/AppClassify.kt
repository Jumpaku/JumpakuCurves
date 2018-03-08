package jumpaku.examples.classify

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.jsonArray
import io.vavr.collection.Array
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.json.parseToJson
import jumpaku.fsc.classify.ClassifierOpen4
import jumpaku.fsc.classify.ClassifierPrimitive7
import jumpaku.fsc.classify.classifyResult
import jumpaku.fsc.classify.reference.Circular
import jumpaku.fsc.classify.reference.Elliptic
import jumpaku.fsc.classify.reference.Linear
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fxcomponents.node.curveControl
import jumpaku.fxcomponents.node.onCurveDone
import jumpaku.fxcomponents.view.cubicFsc
import jumpaku.fxcomponents.view.fuzzyCurve
import tornadofx.*
import java.io.File


fun main(vararg args: String) = Application.launch(AppClassify::class.java, *args)

class AppClassify : App(ViewClassify::class)

class ViewClassify : View() {

    override val root: Pane = pane {
        val group = group {  }
        curveControl {
            prefWidth = 1280.0
            prefHeight = 720.0
            onCurveDone { e ->
                clear()
                group.update(FscGenerator().generate(e.data))
            }
        }
    }

    fun Group.update(fsc: BSpline){
        children.clear()
        cubicFsc(fsc) { stroke = Color.BLACK; strokeWidth = 1.0 }
        listOf(
                Linear.of(fsc).reference,
                Circular.of(fsc).reference,
                Elliptic.of(fsc, nSamples = 25).reference
        ).forEachIndexed { i, r ->
                    fuzzyCurve(r) { stroke = Color.hsb(i*120.0, 1.0, 1.0); strokeWidth = 1.0 }
                }
        val r7 = ClassifierPrimitive7(nSamples = 25, nFmps = 15).classify(fsc)
        println(r7)
    }
}

