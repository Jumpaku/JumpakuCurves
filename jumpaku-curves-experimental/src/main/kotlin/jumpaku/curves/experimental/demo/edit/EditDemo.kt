package jumpaku.curves.experimental.demo.edit


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
import jumpaku.curves.experimental.fsc.edit.Editor
import jumpaku.curves.experimental.fsc.edit.FscComponent
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
import java.nio.file.Paths

fun main(vararg args: String) = Application.launch(EditDemo::class.java, *args)


class EditDemo : Application() {

    val width = 1600.0
    val height = 900.0

    var i = 0
    override fun start(primaryStage: Stage) {
        val curveControl = DrawingControl(width, height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) {
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    val s = Settings.generator.generate(it.drawingStroke)
                    val path = Paths.get("./jumpaku-curves-experimental/src/test/resources/jumpaku/curves/experimental/fsc/test/edit")
                    //println(Paths.get("./jumpaku-curves-experimental/src/test/resources/jumpaku/curves/experimental").toAbsolutePath().toFile().exists())
                    path.resolve("EditingFsc${i++}.json").toFile().writeText(s.toJsonString())
                    val updated = EditDemoModel.update(s)
                    drawFscComponents(updated)
                }
            }
        }
        primaryStage.apply {
            scene = Scene(curveControl).apply {
                addEventHandler(KeyEvent.KEY_PRESSED) {
                    when(it.code) {
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
        drawPoints(s.evaluateAll(0.01), style)
    }
    fun Graphics2D.drawFscComponents(components: List<FscComponent>) {
        components.flatMap { it.fragments().map { it.fragment } }.forEach {
            drawFsc(it, DrawStyle())
        }
        components.flatMap { it.connectors() }.forEach {
            drawPoints(it.front + it.body + it.back, DrawStyle(Color.RED, BasicStroke(3f)))
            drawPolyline(Polyline.of(it.front + it.body + it.back), DrawStyle(Color.RED, BasicStroke(3f)))
        }
    }
}

object EditDemoModel {

    private var history: History<List<FscComponent>> = History<List<FscComponent>>().run { exec { emptyList() } }

    fun update(fsc: BSpline): List<FscComponent> {
        history = history.exec {
            it.map { Settings.editor.edit(fsc, it) }.orDefault { emptyList() }
        }
        return history.current.orThrow()
    }
    fun initialize() {
        history = History<List<FscComponent>>().run { exec { emptyList() } }
    }
}


object Settings {

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
                    necessity = 0.4,
                    possibility = 0.7),
            chunkSize = 8,
            minStayTimeSpan = 0.05)
    val editor: Editor = Editor(
            nConnectorSamples = 17,
            connectionThreshold = Grade.FALSE,
            blender = { exist, overlap -> blender.blend(exist, overlap).map { blendGenerator.generate(it) } },
            fragmenter = { merged -> fragmenter.fragment(merged) })

}
