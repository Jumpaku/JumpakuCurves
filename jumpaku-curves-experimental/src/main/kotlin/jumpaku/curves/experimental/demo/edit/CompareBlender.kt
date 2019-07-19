package jumpaku.curves.experimental.demo.edit

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.stage.Stage
import jumpaku.commons.control.Option
import jumpaku.commons.control.none
import jumpaku.commons.control.optionWhen
import jumpaku.commons.control.some
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.WeightedParamPoint
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.experimental.demo.edit.BlendDemoSettings.blendGenerator
import jumpaku.curves.experimental.demo.edit.BlendDemoSettings.blender
import jumpaku.curves.experimental.demo.edit.BlendDemoSettings.blender_old
import jumpaku.curves.experimental.demo.edit.BlendDemoSettings.blender_old2
import jumpaku.curves.experimental.demo.edit.BlendDemoSettings.generator
import jumpaku.curves.experimental.demo.edit.BlendDemoSettings.height
import jumpaku.curves.experimental.demo.edit.BlendDemoSettings.width
import jumpaku.curves.fsc.blend.BlendGenerator
import jumpaku.curves.fsc.blend.Blender
import jumpaku.curves.fsc.blend.OverlapMatrix
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.generate.extendBack
import jumpaku.curves.fsc.generate.extendFront
import jumpaku.curves.graphics.*
import jumpaku.curves.graphics.fx.DrawingControl
import jumpaku.curves.graphics.fx.DrawingEvent
import org.jfree.fx.FXGraphics2D
import java.awt.Color
import java.awt.Graphics2D


fun main(vararg args: String) = Application.launch(BlendDemo::class.java, *args)

typealias Blender_Old = jumpaku.curves.experimental.demo.edit.oldblend.Blender

private object BlendDemoSettings {

    val width = 600.0

    val height = 850.0

    val generator: Generator = Generator(
            degree = 3,
            knotSpan = 0.1,
            fillSpan = 0.05,
            extendInnerSpan = 0.15,
            extendOuterSpan = 0.15,
            extendDegree = 2,
            fuzzifier = Fuzzifier.Linear(
                    velocityCoefficient = 0.0085,
                    accelerationCoefficient = 0.007
            ))

    val blender_old: Blender_Old = Blender_Old(
            samplingSpan = 0.01,
            blendingRate = 0.5,
            possibilityThreshold = Grade.FALSE)

    val blender: Blender = Blender(
            samplingSpan = 0.01,
            blendingRate = 0.5,
            threshold = Grade.FALSE
    )

    object blender_old2 {
        fun blend(existing: BSpline, overlapping: BSpline): Option<List<WeightedParamPoint>> = blender.run {
            val existSamples = existing.sample(samplingSpan)
            val overlapSamples = overlapping.sample(samplingSpan)
            val osm = OverlapMatrix.create(existSamples.map { it.point }, overlapSamples.map { it.point })
            val overlap = detectOverlap(osm)
            optionWhen(!overlap.isEmpty()) { resample(existSamples, overlapSamples, overlap) }
                    .map { it.aggregated }
        }
    }

    val blendGenerator: BlendGenerator = BlendGenerator(
            degree = 3,
            knotSpan = 0.1,
            bandWidth = 0.01,
            extendInnerSpan = 0.15,
            extendOuterSpan = 0.15,
            extendDegree = 2,
            fuzzifier = Fuzzifier.Linear(
                    velocityCoefficient = 0.0085,
                    accelerationCoefficient = 0.007
            ))
}

class BlendDemo : Application() {

    var existingFscOpt: Option<BSpline> = none()

    override fun start(primaryStage: Stage) {
        val canvasOld = Canvas(width, height)
        val gOld = FXGraphics2D(canvasOld.graphicsContext2D)
        val canvasNew = Canvas(width, height)
        val gNew = FXGraphics2D(canvasNew.graphicsContext2D)
        val curveControl = DrawingControl(width, height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) { event ->
                fun Graphics2D.drawFsc(fsc: BSpline, style: DrawStyle) {
                    drawCubicBSpline(fsc, style)
                    drawPoints(fsc.evaluateAll(0.01), style)
                }
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    existingFscOpt.forEach { drawFsc(it, DrawStyle()) }

                    val overlappingFsc = generator.generate(event.drawingStroke.inputData)

                    existingFscOpt.ifPresent { existingFsc ->
                        blender.blend(existingFsc, overlappingFsc).forEach {
                            existingFscOpt = some(blendGenerator.generate(it))
                        }
                        blender_old2.blend(existingFsc, overlappingFsc).ifPresent {
                            gOld.clearRect(0.0, 0.0, width, height)
                            fun generate(data: List<WeightedParamPoint>): BSpline {
                                val sorted = data.sortedBy { it.param }
                                val domain = Interval(sorted.first().param, sorted.last().param)
                                val prepared = sorted
                                        .let { extendBack(it, generator.extendInnerSpan, generator.extendOuterSpan, generator.extendDegree) }
                                        .let { extendFront(it, generator.extendInnerSpan, generator.extendOuterSpan, generator.extendDegree) }
                                val domainExtended = Interval(prepared.first().param, prepared.last().param)
                                val kv = KnotVector.clamped(domainExtended, generator.degree, generator.knotSpan)
                                return Generator.generate(prepared, kv, generator.fuzzifier).restrict(domain)
                            }
                            gOld.drawFsc(generate(it), DrawStyle(Color.MAGENTA))
                        }
                    }.ifAbsent {
                        existingFscOpt = some(overlappingFsc)
                    }

                    drawFsc(overlappingFsc, DrawStyle(Color.CYAN))
                    existingFscOpt.forEach {
                        drawFsc(it, DrawStyle(Color.MAGENTA))
                        gNew.clearRect(0.0, 0.0, width, height)
                        gNew.drawFsc(it, DrawStyle(Color.MAGENTA))
                    }

                }
            }
        }


        val scene = Scene(HBox(canvasOld, canvasNew, curveControl)).apply {
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
