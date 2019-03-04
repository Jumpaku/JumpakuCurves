package jumpaku.curves.demo.fragment

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import jumpaku.curves.fsc.fragment.Fragment
import jumpaku.curves.fsc.fragment.Fragmenter
import jumpaku.curves.fsc.generate.DataPreparer
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.generate.LinearFuzzifier
import jumpaku.curves.graphics.DrawStyle
import jumpaku.curves.graphics.clearRect
import jumpaku.curves.graphics.drawCubicBSpline
import jumpaku.curves.graphics.drawPoints
import jumpaku.curves.graphics.fx.DrawingControl
import jumpaku.curves.graphics.fx.DrawingEvent
import java.awt.Color


fun main(vararg args: String) = Application.launch(FragmentDemo::class.java, *args)

object FragmentDemoSettings {

    val width = 600.0

    val height = 480.0

    val generator: Generator = Generator(
            degree = 3,
            knotSpan = 0.05,
            preparer = DataPreparer(
                    spanShouldBeFilled = 0.025,
                    extendInnerSpan = 0.075,
                    extendOuterSpan = 0.075,
                    extendDegree = 2),
            fuzzifier = LinearFuzzifier(
                    velocityCoefficient = 0.025,
                    accelerationCoefficient = 0.001
            ))

    val fragmenter: Fragmenter = Fragmenter(
            threshold = Fragmenter.Threshold(
                    necessity = 0.3,
                    possibility = 0.5),
            chunkSize = 4,
            minStayTime = 0.1)
}

class FragmentDemo : Application() {

    override fun start(primaryStage: Stage) {
        val curveControl = DrawingControl(FragmentDemoSettings.width, FragmentDemoSettings.height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) {
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    val fsc = FragmentDemoSettings.generator.generate(it.drawingStroke.paramPoints)
                    val fragments = FragmentDemoSettings.fragmenter.fragment(fsc)
                    fragments.filter { it.type == Fragment.Type.Move }.map { fsc.restrict(it.interval) }.apply {
                        println(size)
                    }.forEach {
                        drawPoints(it.evaluateAll(0.01))
                        drawCubicBSpline(it, DrawStyle(Color.MAGENTA))
                    }
                }
            }
        }
        primaryStage.apply {
            scene = Scene(curveControl)
            show()
        }
    }
}