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
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.polyline.LineSegment
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.lerp
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
import kotlin.math.pow


fun main(vararg args: String) = Application.launch(BlendDemo::class.java, *args)

object BlendDemoSettings {

    val width = 600.0

    val height = 850.0

    val generator: Generator = Generator(
            degree = 3,
            knotSpan = 0.1,
            dataPreparer = DataPreparer(
                    fillSpan = 0.1,
                    extendInnerSpan = 0.1,
                    extendOuterSpan = 0.1,
                    extendDegree = 2),
            fuzzifier = Fuzzifier.Linear(
                    velocityCoefficient = 0.008,
                    accelerationCoefficient = 0.007
            ))

    val blender: Blender = Blender(
            samplingSpan = 0.01,
            blendingRate = 0.5,
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
                    //existingFscOpt.forEach { drawFsc(it, DrawStyle()) }

                    val overlappingFsc = BlendDemoSettings.generator.generate(event.drawingStroke.inputData)

                    existingFscOpt.ifPresent { existingFsc ->
                        Blender2(samplingSpan = blender.samplingSpan, blendingRate = blender.blendingRate, possibilityThreshold = blender.possibilityThreshold, bandWidth = blender.samplingSpan*3) .apply {
                            val existSamples = existingFsc.sample(blender.samplingSpan)
                            val overlapSamples = overlappingFsc.sample(blender.samplingSpan)
                            val osm = OverlapMatrix.create(existSamples.map { it.point }, overlapSamples.map { it.point })
                            val overlap = detectOverlap(osm)
                            for (i in 0 until osm.rowSize) { for (j in 0 until osm.columnSize) {
                                color = Color.getHSBColor(240f/360, overlap.osm[i, j].value.toFloat().pow(2), 1f)
                                fillOval(j*2, i*2, 2, 2)
                            } }
                            overlap.pairs.forEach { (i, j) ->
                                color = Color.getHSBColor(120f/360, overlap.osm[i, j].value.toFloat().pow(2), 1f)
                                fillOval(j*2, i*2, 2, 2)
                            }
                            overlap.path.forEach { (i, j) ->
                                color = Color.getHSBColor(0f, overlap.osm[i, j].value.toFloat().pow(2), 1f)
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
                                    .forEach {
                                        color = Color.ORANGE
                                        fillOval(0, it*2, 2, 2)
                                    }
                                    //.map { existSamples[it].run { copy(param = param + blender.blendingRate * (oBegin - eBegin)) } }
                            val eBack = (existSamples.lastIndex downTo (endI + 1))
                                    .takeWhile { it to overlapSamples.lastIndex !in q }
                                    .forEach {
                                        color = Color.ORANGE
                                        fillOval(overlapSamples.lastIndex*2, it*2, 2, 2)
                                    }
                                    //.map { existSamples[it].run { copy(param = param + blender.blendingRate * (oEnd - eEnd)) } }
                            val oFront = (0 until beginJ)
                                    .takeWhile { 0 to it !in q }
                                    .forEach {
                                        color = Color.ORANGE
                                        fillOval(it*2, 0, 2, 2)
                                    }
                                    //.map { overlapSamples[it].run { copy(param = param - (1 - blender.blendingRate) * (oBegin - eBegin)) } }
                            val oBack = (overlapSamples.lastIndex downTo (endJ + 1))
                                    .takeWhile { existSamples.lastIndex to it !in q }
                                    .forEach {
                                        color = Color.ORANGE
                                        fillOval(it*2, existSamples.lastIndex*2, 2, 2)
                                    }
                                    //.map { overlapSamples[it].run { copy(param = param - (1 - blender.blendingRate) * (oEnd - eEnd)) } }
                            //fillPoints(eFront.map { it.point }, FillStyle(Color.RED))
                            //fillPoints(eBack.map { it.point }, FillStyle(Color.ORANGE))
                            //fillPoints(oFront.map { it.point }, FillStyle(Color.BLUE))
                            //fillPoints(oBack.map { it.point }, FillStyle(Color.GREEN))
                        }.blend(existingFsc, overlappingFsc).forEach {
                            val a = it.first().param
                            val b = it.last().param
                            val maxW = it.maxBy { it.weight }!!.weight
                            fillPoints(it.mapIndexed { i, (tp, w) -> Point.xyr(50.0.lerp((tp.param - a)/(b - a), width - 50), height - 50 -100*w/maxW, 3.0) }, FillStyle(Color.BLACK))
                            drawLineSegment(LineSegment(Point.xy(50.0, height-50), Point.xy(width-50, height-50)))
                            drawLineSegment(LineSegment(Point.xy(50.0, height-150), Point.xy(width-50, height-150)))
                            it.forEach { fillPoint(it.point.copy(r = 1.0), FillStyle(color = Color.getHSBColor(0f, ((it.weight).toFloat()/2+.5f).coerceIn(0f..1f), 0f))) }
                            existingFscOpt = some(BlendDemoSettings.generator.generate(it))
                        }
                        Blender2(samplingSpan = blender.samplingSpan, blendingRate = blender.blendingRate, possibilityThreshold = blender.possibilityThreshold, bandWidth = 1e-10).blend(existingFsc, overlappingFsc).ifPresent {
                            gOld.clearRect(0.0, 0.0, width, height)
                            gOld.drawFsc(generator.generate(it), DrawStyle(Color.MAGENTA))
                        }
                    }.ifAbsent {
                        existingFscOpt = some(overlappingFsc)
                    }

                    //drawFsc(overlappingFsc, DrawStyle(Color.CYAN))
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
