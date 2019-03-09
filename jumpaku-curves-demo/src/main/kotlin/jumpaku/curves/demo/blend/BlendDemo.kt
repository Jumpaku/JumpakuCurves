package jumpaku.curves.demo.blend

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import jumpaku.commons.control.*
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.blend.Blender
import jumpaku.curves.fsc.blend.OverlapMatrix
import jumpaku.curves.fsc.generate.DataPreparer
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.generate.LinearFuzzifier
import jumpaku.curves.graphics.*
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
            preparer = DataPreparer(
                    spanShouldBeFilled = 0.0375,
                    extendInnerSpan = 0.075,
                    extendOuterSpan = 0.075,
                    extendDegree = 2),
            fuzzifier = LinearFuzzifier(
                    velocityCoefficient = 0.025,
                    accelerationCoefficient = 0.001
            ))

    val blender: Blender = Blender(
            samplingSpan = 0.01,
            blendingRate = 0.5,
            minPossibility = Grade(1e-10),
            evaluatePath = { path, osm -> path.grade.value })
}

class BlendDemo : Application() {

    var existingFscOpt: Option<BSpline> = none()

    override fun start(primaryStage: Stage) {
        val curveControl = DrawingControl(BlendDemoSettings.width, BlendDemoSettings.height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) {
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    existingFscOpt.forEach { drawCubicBSpline(it) }
                    existingFscOpt.ifAbsent { drawPoints(it.drawingStroke.inputData.map { it.point.copy(r = 3.0) }) }
                    //existingFscOpt.forEach { drawPoints(it.evaluateAll(0.01)) }

                    val overlappingFsc = BlendDemoSettings.generator.generate(it.drawingStroke.inputData)

                    existingFscOpt.forEach { existing ->
                        val samplingSpan = BlendDemoSettings.blender.samplingSpan
                        val existSamples = existing.sample(samplingSpan)
                        val overlapSamples = overlappingFsc.sample(samplingSpan)
                        val osm = OverlapMatrix.create(existSamples.map { it.point }, overlapSamples.map { it.point })
                        val paths = BlendDemoSettings.blender.findPaths(osm, BlendDemoSettings.blender.minPossibility)
                        val path = paths.maxBy { BlendDemoSettings.blender.evaluatePath(it, osm) }.toOption()
                        fun point(i: Int, j: Int): Point = Point.xyr(i*5.0, j*5.0, 1.0)
                        for (y in 0..osm.rowLastIndex) {
                            for (x in 0..osm.columnLastIndex) {
                                drawPoint(point(y, x), DrawStyle(
                                        color = if (osm[y, x] >= BlendDemoSettings.blender.minPossibility) Color.BLUE
                                        else Color.LIGHT_GRAY))
                            }
                        }
                        path.forEach {
                            it.map { (y, x) -> point(y, x) }.forEach { drawPoint(it, DrawStyle(Color.RED)) }
                            drawPoints(((0 until it.first().first) + ((it.last().first + 1) until existSamples.size)).map { i -> existSamples[i].point.copy(r = 3.0) }, DrawStyle(Color.GREEN))
                            drawPoints(((0 until it.first().second) + ((it.last().second + 1) until overlapSamples.size)).map { j -> overlapSamples[j].point.copy(r = 3.0) }, DrawStyle(Color.GREEN))
                            drawPoints(it.map { (i, j) -> existSamples[i].lerp(BlendDemoSettings.blender.blendingRate, overlapSamples[j]).point.copy(r = 3.0) }, DrawStyle(Color.BLUE))
                        }
                    }

                    existingFscOpt.ifPresent { existingFsc ->
                        BlendDemoSettings.blender.blend(existingFsc, overlappingFsc).forEach {
                            existingFscOpt = some(BlendDemoSettings.generator.generate(it))
                        }
                    }.ifAbsent {
                        existingFscOpt = some(overlappingFsc)
                    }

                    //drawCubicBSpline(overlappingFsc, DrawStyle(Color.CYAN))
                    //drawPoints(overlappingFsc.evaluateAll(0.01), DrawStyle(Color.CYAN))
                    existingFscOpt.forEach { drawCubicBSpline(it, DrawStyle(Color.MAGENTA)) }
                    existingFscOpt.forEach { drawPoints(it.evaluateAll(0.01), DrawStyle(Color.MAGENTA)) }
                }
            }
        }
        val scene = Scene(curveControl).apply {
            addEventHandler(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.C) {
                    curveControl.updateGraphics2D { clearRect(0.0, 0.0, width, height) }
                    existingFscOpt = none()
                }
            }
        }
        primaryStage.scene = scene
        primaryStage.show()
    }
}
