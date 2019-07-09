package jumpaku.curves.demo

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.geom.Point
import jumpaku.curves.graphics.*
import jumpaku.curves.graphics.fx.DrawingControl
import jumpaku.curves.graphics.fx.DrawingEvent
import java.awt.Color
import kotlin.math.sqrt


fun main(vararg args: String) = Application.launch(FxTemplate::class.java, *args)

class FxTemplate : Application() {

    val width = 600.0
    val height = 480.0

    override fun start(primaryStage: Stage) {
        val curveControl = DrawingControl(width, height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) {
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    drawPolyline(Polyline(it.drawingStroke.inputData))
                    val w=-sqrt(2.0)/2
                    val r = sqrt((w + 1)/2)
                    val t = 1/(2*r + 2)
                    println(t)
                    fun u(h: Double): Double = ((h - t)/(1 - 2*t)).apply { println(h) }
                    fun b(s: Double, cs: ConicSection): Point {
                        println(s)
                        return when(s) {
                            in t..(1-t) -> cs(u(s))
                            else -> cs.complement()(u(s)/(2*u(s) - 1))
                        }
                    }
                    val cs = ConicSection(Point.xyr(200.0, 200.0, 1.0), Point.xyr(300.0, 100.0, 1.0), Point.xyr(400.0, 200.0, 1.0), w)
                    drawConicSection(cs)
                    val ts = cs.domain.sample(20)
                    drawPoints(ts.map(cs))
                    val compl = cs.restrict(t, 1-t).complement()
                    drawPoints(ts.map { b(it, cs.restrict(t, 1-t)) }, DrawStyle(color = Color.RED))
                    drawPoints(listOf(1/(2*r+2), (2*r+1)/(2*r+2)).map { compl(it) }, DrawStyle(color = Color.GREEN))

                    val a = (1-t)*(1-t)*w + 2*t*(1-t) + t*t*w
                    val b = (1-t)*(1-t) + 2*t*(1-t)*w + t*t
                    println(a/b)
                    println(cs.restrict(t, 1-t).weight)
                }
            }
        }
        primaryStage.apply {
            scene = Scene(curveControl)
            show()
        }
    }
}