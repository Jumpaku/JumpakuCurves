package jumpaku.curves.demo.blend

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import jumpaku.commons.control.Option
import jumpaku.commons.control.none
import jumpaku.commons.control.some
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.blend.Blender
import jumpaku.curves.fsc.generate.DataPreparer
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.graphics.DrawStyle
import jumpaku.curves.graphics.clearRect
import jumpaku.curves.graphics.drawCubicBSpline
import jumpaku.curves.graphics.drawPoints
import jumpaku.curves.graphics.fx.DrawingControl
import jumpaku.curves.graphics.fx.DrawingEvent
import java.awt.Color


fun main(vararg args: String) = Application.launch(BlendDemo::class.java, *args)

object BlendDemoSettings {

    val width = 600.0

    val height = 480.0

    val generator: Generator = Generator(
            degree = 3,
            knotSpan = 0.075,
            dataPreparer = DataPreparer(
                    fillSpan = 0.0375,
                    extendInnerSpan = 0.075,
                    extendOuterSpan = 0.075,
                    extendDegree = 2),
            fuzzifier = Fuzzifier.Linear(
                    velocityCoefficient = 0.025,
                    accelerationCoefficient = 0.001
            ))

    val blender: Blender = Blender(
            samplingSpan = 0.01,
            blendingRate = 0.65,
            possibilityThreshold = Grade(1e-10))
}

class BlendDemo : Application() {

    var existingFscOpt: Option<BSpline> = none()

    override fun start(primaryStage: Stage) {
        val curveControl = DrawingControl(BlendDemoSettings.width, BlendDemoSettings.height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) {
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    existingFscOpt.forEach { drawCubicBSpline(it) }
                    existingFscOpt.forEach { drawPoints(it.evaluateAll(0.01)) }

                    val overlappingFsc = BlendDemoSettings.generator.generate(it.drawingStroke.inputData)

                    existingFscOpt.ifPresent { existingFsc ->
                        BlendDemoSettings.blender.blend(existingFsc, overlappingFsc).forEach {
                            existingFscOpt = some(BlendDemoSettings.generator.generate(it))
                        }
                    }.ifAbsent {
                        existingFscOpt = some(overlappingFsc)
                    }

                    drawCubicBSpline(overlappingFsc, DrawStyle(Color.CYAN))
                    drawPoints(overlappingFsc.evaluateAll(0.01), DrawStyle(Color.CYAN))
                    existingFscOpt.forEach { drawCubicBSpline(it, DrawStyle(Color.MAGENTA)) }
                    existingFscOpt.forEach { drawPoints(it.evaluateAll(0.01), DrawStyle(Color.MAGENTA)) }
                }
            }
        }
        val scene = Scene(curveControl).apply {
            addEventHandler(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.C) {//clear
                    curveControl.updateGraphics2D { clearRect(0.0, 0.0, width, height) }
                    existingFscOpt = none()
                }
            }
        }
        primaryStage.scene = scene
        primaryStage.show()
    }
}
