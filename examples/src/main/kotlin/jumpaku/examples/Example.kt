package jumpaku.examples

import io.vavr.collection.Array
import javafx.application.Application
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Shape
import jumpaku.core.curve.Interval
import jumpaku.core.curve.arclength.Reparametrizer
import jumpaku.core.curve.arclength.approximateParams
import jumpaku.core.curve.arclength.repeatBisect
import jumpaku.core.curve.bezier.Bezier
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.geom.Point
import jumpaku.core.transform.Translate
import jumpaku.core.transform.UniformlyScale
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fxcomponents.nodes.curveControl
import jumpaku.fxcomponents.nodes.fuzzyPoints
import jumpaku.fxcomponents.nodes.onCurveDone
import org.apache.commons.math3.util.FastMath
import tornadofx.*
import kotlin.math.sqrt

fun BSpline.approximate(): Array<Double> = repeatBisect(clamp(), domain)
{
    val cp = restrict(it).controlPoints
    val l0 = Polyline(cp).reparameterized.domain.end
    val l1 = cp.run { head().dist(last()) }
    FastMath.abs(l0 - l1) > 1.0/128
} .map { it.begin }.append(domain.end).toArray()

fun main(vararg args: String) = Application.launch(AppExample::class.java, *args)

class AppExample : App(ViewExample::class)

class ViewExample : View() {
    override val root: Pane = pane {
        val group = group {}
        curveControl {
            prefWidth = 1280.0
            prefHeight = 720.0
            onCurveDone {
                clear()
                val s = FscGenerator().generate(it.data)
                val r = Reparametrizer.of(s, s.domain.sample(10000))
                val l = r.range.end
                fun plot(p: Point): Point = p.copy(x = (p.x - s.domain.begin)/s.domain.span*1000 + 50, y = 720 - p.y/l*600 - 50)
                fun approximate(b: BSpline): Array<Double> {
                    return b.domain.sample(b.knotVector.knots.count { (v, _) -> v in b.domain }*60/60)
                }
                with(group) {
                    children.clear()
                    fun doArcLength(ts: Array<Double>, op: Circle.()->Unit) {
                        Reparametrizer.ofL(s, ts).run {
                            println("${ts.size()},\t${range.end}")
                            fuzzyPoints(range.sample(1000).map { plot(Point.xy(toOriginal(it), it)) }, op)
                            fuzzyPoints(range.sample(50).map { s(toOriginal(it)).copy(r = 2.0) }, op)
                        }
                    }
                    r.run {
                        println(range.end)
                        fuzzyPoints(domain.sample(50).map { s(it).copy(r = 1.0) }) { stroke = Color.BLACK }
                        fuzzyPoints(originalParams.map { plot(Point.xy(it, toArcLength(it))) }) { stroke = Color.RED }
                        fuzzyPoints(range.sample(50).map { s(toOriginal(it)).copy(r = 3.0) }) { stroke = Color.RED }
                    }
                    doArcLength(approximate(s)) { stroke = Color.BLUE}
                    doArcLength(s.domain.sample(100)) { stroke = Color.GREEN }
                    println()
                }
            }
        }
    }
}