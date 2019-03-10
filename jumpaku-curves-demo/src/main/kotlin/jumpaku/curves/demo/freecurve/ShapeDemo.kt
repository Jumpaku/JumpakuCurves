package jumpaku.curves.demo.freecurve

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import jumpaku.curves.fsc.freecurve.Segmenter
import jumpaku.curves.fsc.freecurve.Shaper
import jumpaku.curves.fsc.freecurve.Smoother
import jumpaku.curves.fsc.generate.DataPreparer
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.identify.primitive.Open4Identifier
import jumpaku.curves.graphics.*
import jumpaku.curves.graphics.fx.DrawingControl
import jumpaku.curves.graphics.fx.DrawingEvent
import java.awt.Color


fun main(vararg args: String) = Application.launch(ShapeDemo::class.java, *args)

object ShapeDemoSettings {

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

    val shaper: Shaper = Shaper(
            segmenter = Segmenter(Open4Identifier(
                    nSamples = 25,
                    nFmps = 15)),
            smoother = Smoother(
                    pruningFactor = 2.0,
                    nFitSamples = 33,
                    fscSampleSpan = 0.02),
            sampleFsc = { fsc -> fsc.domain.sample(100)})
}

class ShapeDemo : Application() {

    override fun start(primaryStage: Stage) {
        val curveControl = DrawingControl(ShapeDemoSettings.width, ShapeDemoSettings.height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) {
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    val fsc = ShapeDemoSettings.generator.generate(it.drawingStroke.inputData)
                    val (_, _, smooth) = ShapeDemoSettings.shaper.shape(fsc)
                    drawPoints(fsc.evaluateAll(0.01))
                    smooth.conicSections.forEach { drawConicSection(it, DrawStyle(Color.MAGENTA)) }
                    smooth.cubicBeziers.forEach { drawCubicBezier(it, DrawStyle(Color.CYAN)) }
                }
            }
        }
        primaryStage.apply {
            scene = Scene(curveControl)
            show()
        }
    }
}