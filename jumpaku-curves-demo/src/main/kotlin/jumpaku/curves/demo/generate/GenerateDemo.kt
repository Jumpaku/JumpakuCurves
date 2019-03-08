package jumpaku.curves.demo.generate

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import jumpaku.curves.fsc.generate.DataPreparer
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.generate.LinearFuzzifier
import jumpaku.curves.graphics.clearRect
import jumpaku.curves.graphics.drawCubicBSpline
import jumpaku.curves.graphics.drawPoints
import jumpaku.curves.graphics.fx.DrawingControl
import jumpaku.curves.graphics.fx.DrawingEvent


fun main(vararg args: String) = Application.launch(GenerateDemo::class.java, *args)

object GenerateDemoSettings {

    val width = 600.0

    val height = 480.0

    val generator: Generator = Generator(
            degree = 3,
            knotSpan = 0.075,
            preparer = DataPreparer(
                    spanShouldBeFilled = 0.0375,
                    extendInnerSpan = 0.075,
                    extendOuterSpan = 0.075,
                    extendDegree = 2),
            fuzzifier = LinearFuzzifier(
                    velocityCoefficient = 0.025,
                    accelerationCoefficient = 0.001
            ))
}

class GenerateDemo : Application() {

    override fun start(primaryStage: Stage) {
        val curveControl = DrawingControl(GenerateDemoSettings.width, GenerateDemoSettings.height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) {
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    val fsc = GenerateDemoSettings.generator.generate(it.drawingStroke.inputData)
                    drawCubicBSpline(fsc)
                    drawPoints(fsc.evaluateAll(0.01))
                }
            }
        }
        primaryStage.apply {
            scene = Scene(curveControl)
            show()
        }
    }
}