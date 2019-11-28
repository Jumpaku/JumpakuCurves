package jumpaku.curves.demo.experimental.edit

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.blend.BlendGenerator
import jumpaku.curves.fsc.blend.Blender
import jumpaku.curves.fsc.experimental.edit.Editor
import jumpaku.curves.fsc.experimental.edit.FscGraph
import jumpaku.curves.fsc.experimental.edit.Merger
import jumpaku.curves.fsc.fragment.Chunk
import jumpaku.curves.fsc.fragment.Fragmenter
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.graphics.*
import jumpaku.curves.graphics.fx.DrawingControl
import jumpaku.curves.graphics.fx.DrawingEvent
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.nio.file.Paths

fun main(vararg args: String) = Application.launch(EditDemo::class.java, *args)


class EditDemo : Application() {

    val width = 1600.0
    val height = 900.0

    var fscGraph: FscGraph = FscGraph.of()

    override fun start(primaryStage: Stage) {
        println(Paths.get(".").toAbsolutePath())
        val curveControl = DrawingControl(width, height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) {
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    val fsc = Settings.generator.generate(it.drawingStroke)
                    fscGraph = Settings.editor.edit(fscGraph, fsc)
                    drawFscGraph(fscGraph)
                }
            }
        }
        primaryStage.apply {
            scene = Scene(curveControl).apply {
                addEventHandler(KeyEvent.KEY_PRESSED) {
                    when (it.code) {
                        KeyCode.C -> {
                            fscGraph = FscGraph.of()
                            curveControl.updateGraphics2D { clearRect(0.0, 0.0, width, height) }
                        }
                    }
                }
            }
            show()
        }
    }

    fun Graphics2D.drawFsc(s: BSpline, style: DrawStyle) {
        drawCubicBSpline(s, style)
        drawPoints(s.evaluateAll(0.05 / 4), style)
    }

    fun Graphics2D.drawConnectors(graph: FscGraph, style: DrawStyle = DrawStyle()) {
        graph.decompose().flatMap { it.connectors() }.forEach {
            drawPoints(it.front + it.body + it.back, style)
            drawPolyline(Polyline.byIndices(it.front + it.body + it.back), style)
        }
    }

    fun Graphics2D.drawTargets(graph: FscGraph, style: DrawStyle = DrawStyle()) {
        graph.decompose().flatMap { it.fragments().map { it.fragment } }.forEach {
            drawFsc(it, style)
        }
    }

    fun Graphics2D.drawFscGraph(graph: FscGraph) {
        drawTargets(graph, DrawStyle())
        drawConnectors(graph, DrawStyle(Color.RED, BasicStroke(3f)))
    }
}

private object Settings {

    val generator: Generator = Generator(
            degree = 3,
            knotSpan = 0.1,
            fillSpan = 0.025,
            extendInnerSpan = 0.15,
            extendOuterSpan = 0.15,
            extendDegree = 2,
            fuzzifier = Fuzzifier.Linear(
                    velocityCoefficient = 0.0085,
                    accelerationCoefficient = 0.007
            )
    )
    val blender = Blender(
            samplingSpan = 0.01,
            blendingRate = 0.5,
            threshold = Grade.FALSE)
    val blendGenerator = BlendGenerator(
            degree = generator.degree,
            knotSpan = generator.knotSpan,
            bandWidth = blender.samplingSpan,
            extendInnerSpan = generator.extendInnerSpan,
            extendOuterSpan = generator.extendOuterSpan,
            extendDegree = generator.extendDegree,
            fuzzifier = generator.fuzzifier)
    val fragmenter = Fragmenter(
            threshold = Chunk.Threshold(
                    necessity = 0.45,
                    possibility = 0.75),
            chunkSize = 4,
            minStayTimeSpan = 0.05)
    val editor: Editor = Editor(
            nConnectorSamples = 17,
            connectionThreshold = Grade.FALSE,
            merger = Merger(blender, blendGenerator),
            fragmenter = fragmenter)
}
