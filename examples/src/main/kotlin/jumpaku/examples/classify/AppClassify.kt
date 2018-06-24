package jumpaku.examples.classify

import io.vavr.collection.Array
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.Interval
import jumpaku.core.curve.arclength.ReparametrizedCurve
import jumpaku.core.curve.bspline.BSpline
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fsc.identify.IdentifyResult
import jumpaku.fsc.identify.Open4Identifier
import jumpaku.fsc.identify.Primitive7Identifier
import jumpaku.fsc.identify.reparametrize
import jumpaku.fxcomponents.nodes.*
import tornadofx.App
import tornadofx.View
import tornadofx.group
import tornadofx.pane
import kotlin.math.log
import kotlin.math.roundToInt
import kotlin.system.measureNanoTime


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
        cubicSpline(fsc) { stroke = Color.BLACK; strokeWidth = 1.0 }
        val s = reparametrize(fsc)
        val r7 = Open4Identifier(nSamples = 25, nFmps = 15).identify(s)
        println(r7.grades)
        fuzzyPoints(s.evaluateAll(15)) { stroke = Color.BLACK; strokeWidth = 1.0 }

        listOf(r7.circular).forEachIndexed { i, r ->
            fuzzyPoints(r.reparametrized.evaluateAll(15)) { stroke = Color.hsb(i * 120.0, 1.0, 1.0); strokeWidth = 1.0 }
        }
        listOf(r7.circular).forEachIndexed { i, r ->
            fuzzyCurve(r.toCrisp(), 20.0) { stroke = Color.hsb(i * 120.0, 1.0, 1.0); strokeWidth = 4.0 }
        }
        r7.circular.base.run {
            center().forEach {
                println(it.dist(begin))
                println(it.dist(far))
                println(it.dist(end))
                println(far.dist(begin) - far.dist(end))
                println()
            }
        }
        val l = mutableListOf<IdentifyResult>()
        val time = measureNanoTime {
            val s = reparametrize(fsc)
            repeat((200* log(200.0, 2.0).roundToInt())) {
                l += Open4Identifier(25, 15).identify(s)
            }
        }
        val ll = mutableListOf<Int>()
        val time2 = measureNanoTime {
            repeat((200* log(200.0, 2.0).roundToInt())) {
                val s = reparametrize(fsc)
                ll += s.reparametrizer.originalParams.size()
                l += Open4Identifier(25, 15).identify(s)
            }
        }
        println(time*1.0e-9)
        println(time2*1.0e-9)
        println(l.size)
        println(ll.last())
        println(fsc.domain.span)
    }
}

