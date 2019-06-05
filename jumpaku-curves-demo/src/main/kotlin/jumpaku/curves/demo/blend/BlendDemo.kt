package jumpaku.curves.demo.blend

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.stage.Stage
import jumpaku.commons.control.Option
import jumpaku.commons.control.none
import jumpaku.commons.control.some
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.demo.blend.BlendDemoSettings.blender
import jumpaku.curves.demo.blend.BlendDemoSettings.generator
import jumpaku.curves.demo.blend.BlendDemoSettings.height
import jumpaku.curves.demo.blend.BlendDemoSettings.width
import jumpaku.curves.fsc.blend.Blender
import jumpaku.curves.fsc.blend.Blender2
import jumpaku.curves.fsc.blend.OverlapMatrix
import jumpaku.curves.fsc.generate.DataPreparer
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.graphics.*
import jumpaku.curves.graphics.fx.DrawingControl
import jumpaku.curves.graphics.fx.DrawingEvent
import org.jfree.fx.FXGraphics2D
import java.awt.Color
import java.awt.Graphics2D


fun main(vararg args: String) = Application.launch(BlendDemo::class.java, *args)

object BlendDemoSettings {

    val width = 600.0

    val height = 850.0

    val generator: Generator = Generator(
            degree = 3,
            knotSpan = 0.1,
            dataPreparer = DataPreparer(
                    fillSpan = 0.0375,
                    extendInnerSpan = 0.075,
                    extendOuterSpan = 0.075,
                    extendDegree = 2),
            fuzzifier = Fuzzifier.Linear(
                    velocityCoefficient = 0.016,
                    accelerationCoefficient = 0.005
            ))

    val blender: Blender = Blender(
            samplingSpan = 0.01,
            blendingRate = 0.6,
            possibilityThreshold = Grade(1e-10))
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

                    val overlappingFsc = BlendDemoSettings.generator.generate(event.drawingStroke.inputData)

                    existingFscOpt.ifPresent { existingFsc ->
                        Blender2(blender).apply {
                            val existSamples = existingFsc.sample(blender.samplingSpan)
                            val overlapSamples = overlappingFsc.sample(blender.samplingSpan)
                            val osm = OverlapMatrix.create(existSamples.map { it.point }, overlapSamples.map { it.point })
                            val overlap = detectOverlap(osm)
                            for (i in 0 until osm.rowSize) { for (j in 0 until osm.columnSize) {
                                color = Color.getHSBColor(240f/360, overlap.osm[i, j].value.toFloat(), 1f)
                                fillOval(j*2, i*2, 2, 2)
                            } }
                            overlap.pairs.forEach { (i, j) ->
                                color = Color.getHSBColor(120f/360, overlap.osm[i, j].value.toFloat(), 1f)
                                fillOval(j*2, i*2, 2, 2)
                            }
                            overlap.path.forEach { (i, j) ->
                                color = Color.getHSBColor(0f, overlap.osm[i, j].value.toFloat(), 1f)
                                fillOval(j*2, i*2, 2, 2)
                            }
                            val (beginI, beginJ) = overlap.path.first()
                            val (endI, endJ) = overlap.path.last()
                            val eBegin = existSamples[beginI].param
                            val eEnd = existSamples[endI].param
                            val oBegin = overlapSamples[beginJ].param
                            val oEnd = overlapSamples[endJ].param
                            val q = overlap.pairs
                            val eFront = (0 until beginI)
                                    .takeWhile { it to 0 !in q }
                                    .map { existSamples[it].run { copy(param = param + blender.blendingRate * (oBegin - eBegin)) } }
                            val eBack = (existSamples.lastIndex downTo (endI + 1))
                                    .takeWhile { it to overlapSamples.lastIndex !in q }
                                    .map { existSamples[it].run { copy(param = param + blender.blendingRate * (oEnd - eEnd)) } }
                            val oFront = (0 until beginJ)
                                    .takeWhile { 0 to it !in q }
                                    .map { overlapSamples[it].run { copy(param = param - (1 - blender.blendingRate) * (oBegin - eBegin)) } }
                            val oBack = (overlapSamples.lastIndex downTo (endJ + 1))
                                    .takeWhile { existSamples.lastIndex to it !in q }
                                    .map { overlapSamples[it].run { copy(param = param - (1 - blender.blendingRate) * (oEnd - eEnd)) } }
                            fillPoints(eFront.map { it.point }, FillStyle(Color.RED))
                            fillPoints(eBack.map { it.point }, FillStyle(Color.ORANGE))
                            fillPoints(oFront.map { it.point }, FillStyle(Color.BLUE))
                            fillPoints(oBack.map { it.point }, FillStyle(Color.GREEN))
                        }.blend(existingFsc, overlappingFsc).forEach {
                            existingFscOpt = some(BlendDemoSettings.generator.generate(it))
                        }
                        blender.blend(existingFsc, overlappingFsc).ifPresent {
                            gOld.clearRect(0.0, 0.0, width, height)
                            gOld.drawFsc(generator.generate(it), DrawStyle(Color.MAGENTA))
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
