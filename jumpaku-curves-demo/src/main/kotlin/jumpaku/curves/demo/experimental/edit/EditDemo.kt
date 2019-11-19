package jumpaku.curves.demo.experimental.edit

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import jumpaku.commons.control.orDefault
import jumpaku.commons.history.History
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.experimental.edit.Editor
import jumpaku.curves.fsc.experimental.edit.FscGraph
import jumpaku.curves.fsc.experimental.edit.FscPath
import jumpaku.curves.fsc.blend.BlendGenerator
import jumpaku.curves.fsc.blend.Blender
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

fun main(vararg args: String) = Application.launch(EditDemo::class.java, *args)


class EditDemo : Application() {

    val width = 1600.0
    val height = 900.0

    override fun start(primaryStage: Stage) {
        val curveControl = DrawingControl(width, height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) {
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    val s = Settings.generator.generate(it.drawingStroke)
                    val updated = EditDemoModel.update(s)
                    drawFscComponents(updated.decompose())
                }
            }
        }
        primaryStage.apply {
            scene = Scene(curveControl).apply {
                addEventHandler(KeyEvent.KEY_PRESSED) {
                    when (it.code) {
                        KeyCode.C -> curveControl.updateGraphics2D {
                            clearRect(0.0, 0.0, width, height)
                            EditDemoModel.initialize()
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

    fun Graphics2D.drawFscComponents(paths: List<FscPath>) {
        paths.flatMap { it.fragments().map { it.fragment } }.forEach {
            drawFsc(it, DrawStyle())
        }
        paths.flatMap { it.connectors() }.forEach {
            drawPoints(it.front + it.body + it.back, DrawStyle(Color.RED, BasicStroke(3f)))
            drawPolyline(Polyline.of(it.front + it.body + it.back), DrawStyle(Color.RED, BasicStroke(3f)))
        }
    }
}

object EditDemoModel {

    private var history: History<FscGraph> = History<FscGraph>().run { exec { FscGraph() } }

    fun update(fsc: BSpline): FscGraph {
        history = history.exec {
            it.map { Settings.editor.edit(fsc, it) }.orDefault { FscGraph() }
        }
        return history.current.orThrow()
    }

    fun initialize() {
        history = History<FscGraph>().run { exec { FscGraph() } }
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
            merger = Editor.mergerOf(blender, blendGenerator),
            fragmenter = Editor.fragmenterOf(fragmenter))
}
