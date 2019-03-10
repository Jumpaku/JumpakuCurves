package jumpaku.curves.demo.identify.primitive

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import jumpaku.curves.fsc.generate.DataPreparer
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.generate.Fuzzifier.Linear
import jumpaku.curves.fsc.identify.primitive.CurveClass.*
import jumpaku.curves.fsc.identify.primitive.Identifier
import jumpaku.curves.fsc.identify.primitive.Open4Identifier
import jumpaku.curves.fsc.identify.primitive.reparametrize
import jumpaku.curves.graphics.*
import jumpaku.curves.graphics.fx.DrawingControl
import jumpaku.curves.graphics.fx.DrawingEvent
import java.awt.Color


fun main(vararg args: String) = Application.launch(IdentifyDemo::class.java, *args)

object IdentifyDemoSettings {

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
            fuzzifier = Fuzzifier.Linear(
                    velocityCoefficient = 0.025,
                    accelerationCoefficient = 0.001
            ))

    val identifier: Identifier = Open4Identifier(nSamples = 25, nFmps = 15)
}

class IdentifyDemo : Application() {

    override fun start(primaryStage: Stage) {
        val curveControl = DrawingControl(IdentifyDemoSettings.width, IdentifyDemoSettings.height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) {
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    val fsc = IdentifyDemoSettings.generator.generate(it.drawingStroke.inputData)
                    val result = IdentifyDemoSettings.identifier.identify(reparametrize(fsc))
                    println("curveClass: ${result.curveClass}")
                    println("grade: ${result.grade}")
                    drawPoints(fsc.evaluateAll(0.01))
                    val resultStyle = DrawStyle(Color.MAGENTA)
                    result.apply {
                        when (curveClass) {
                            LineSegment -> drawConicSection(result.linear.base.toCrisp(), resultStyle)
                            CircularArc -> drawConicSection(result.circular.base.toCrisp(), resultStyle)
                            EllipticArc -> drawConicSection(result.elliptic.base.toCrisp(), resultStyle)
                            OpenFreeCurve -> drawCubicBSpline(fsc.toCrisp(), resultStyle)
                        }
                    }
                }
            }
        }
        primaryStage.apply {
            scene = Scene(curveControl)
            show()
        }
    }
}