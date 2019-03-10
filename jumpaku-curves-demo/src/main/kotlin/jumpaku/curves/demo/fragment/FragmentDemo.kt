package jumpaku.curves.demo.fragment

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import jumpaku.curves.fsc.fragment.Fragment
import jumpaku.curves.fsc.fragment.Fragmenter
import jumpaku.curves.fsc.generate.DataPreparer
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.graphics.DrawStyle
import jumpaku.curves.graphics.clearRect
import jumpaku.curves.graphics.drawCubicBSpline
import jumpaku.curves.graphics.drawPoints
import jumpaku.curves.graphics.fx.DrawingControl
import jumpaku.curves.graphics.fx.DrawingEvent
import java.awt.BasicStroke
import java.awt.Color


fun main(vararg args: String) = Application.launch(FragmentDemo::class.java, *args)

object FragmentDemoSettings {

    val width = 600.0

    val height = 480.0

    val generator: Generator = Generator(
            degree = 3,
            knotSpan = 0.075,
            dataPreparer = DataPreparer(
                    fillSpan = 0.025,
                    extendInnerSpan = 0.075,
                    extendOuterSpan = 0.075,
                    extendDegree = 2),
            fuzzifier = Fuzzifier.Linear(
                    velocityCoefficient = 0.025,
                    accelerationCoefficient = 0.001
            ))

    val fragmenter: Fragmenter = Fragmenter(
            threshold = Fragmenter.Threshold(
                    necessity = 0.35,
                    possibility = 0.65),
            chunkSize = 4,
            minStayTime = 0.04)
}

class FragmentDemo : Application() {

    override fun start(primaryStage: Stage) {
        val curveControl = DrawingControl(FragmentDemoSettings.width, FragmentDemoSettings.height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) {
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    val fsc = FragmentDemoSettings.generator.generate(it.drawingStroke.inputData)
                    val fragments = FragmentDemoSettings.fragmenter.fragment(fsc)
                    println(fragments.count { it.type == Fragment.Type.Move })
                    fragments.filter { it.type == Fragment.Type.Move }.map { fsc.restrict(it.interval) }.apply {
                        forEach { drawPoints(it.evaluateAll(0.01)) }
                        forEach { drawCubicBSpline(it, DrawStyle(Color.MAGENTA, BasicStroke(3f))) }
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