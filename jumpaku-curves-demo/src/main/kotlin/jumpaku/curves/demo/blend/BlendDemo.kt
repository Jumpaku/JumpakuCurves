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
import jumpaku.curves.demo.blend.BlendDemoSettings.blendGenerator
import jumpaku.curves.demo.blend.BlendDemoSettings.blender
import jumpaku.curves.demo.blend.BlendDemoSettings.generator
import jumpaku.curves.demo.blend.BlendDemoSettings.height
import jumpaku.curves.demo.blend.BlendDemoSettings.width
import jumpaku.curves.fsc.blend.BlendGenerator
import jumpaku.curves.fsc.blend.Blender
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.graphics.DrawStyle
import jumpaku.curves.graphics.clearRect
import jumpaku.curves.graphics.drawCubicBSpline
import jumpaku.curves.graphics.drawPoints
import jumpaku.curves.graphics.fx.DrawingControl
import jumpaku.curves.graphics.fx.DrawingEvent
import java.awt.Color
import java.awt.Graphics2D


fun main(vararg args: String) = Application.launch(BlendDemo::class.java, *args)

object BlendDemoSettings {

    val width = 640.0

    val height = 480.0

    val generator: Generator = Generator(
            degree = 4,
            knotSpan = 0.1,
            fillSpan = 0.1 / 3,
            extendInnerSpan = 0.15,
            extendOuterSpan = 0.1,
            extendDegree = 2,
            fuzzifier = Fuzzifier.Linear(
                    velocityCoefficient = 0.008,
                    accelerationCoefficient = 0.007
            ))

    val blender: Blender = Blender(
            samplingSpan = 0.01,
            blendingRate = 0.5,
            threshold = Grade(1e-10))

    val blendGenerator: BlendGenerator = BlendGenerator(generator, blender.samplingSpan)
}

class BlendDemo : Application() {

    var existingFscOpt: Option<BSpline> = none()

    override fun start(primaryStage: Stage) {
        val curveControl = DrawingControl(width, height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) { event ->
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    val overlappingFsc = generator.generate(event.drawingStroke)
                    existingFscOpt.ifPresent { existingFsc ->
                        drawFsc(existingFsc, DrawStyle())
                        drawFsc(overlappingFsc, DrawStyle(Color.CYAN))
                        blender.blend(existingFsc, overlappingFsc).let { (_, blended) ->
                            blended.forEach {
                                existingFscOpt = some(blendGenerator.generate(it))
                            }
                        }
                    }.ifAbsent {
                        existingFscOpt = some(overlappingFsc)
                    }
                    existingFscOpt.forEach { drawFsc(it, DrawStyle(Color.MAGENTA)) }
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

private fun Graphics2D.drawFsc(fsc: BSpline, style: DrawStyle) {
    drawCubicBSpline(fsc, style)
    drawPoints(fsc.evaluateAll(0.01), style)
}