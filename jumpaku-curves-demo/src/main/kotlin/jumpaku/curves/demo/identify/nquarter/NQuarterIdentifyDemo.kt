package jumpaku.curves.demo.identify.nquarter

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.fsc.generate.DataPreparer
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.identify.nquarter.NQuarterClass
import jumpaku.curves.fsc.identify.nquarter.NQuarterIdentifier
import jumpaku.curves.fsc.identify.primitive.CurveClass
import jumpaku.curves.fsc.identify.primitive.Identifier
import jumpaku.curves.fsc.identify.primitive.Open4Identifier
import jumpaku.curves.fsc.identify.primitive.reparametrize
import jumpaku.curves.fsc.snap.conicsection.ConjugateBox
import jumpaku.curves.graphics.*
import jumpaku.curves.graphics.fx.DrawingControl
import jumpaku.curves.graphics.fx.DrawingEvent
import java.awt.Color


fun main(vararg args: String) = Application.launch(NQuarterDemo::class.java, *args)

object NQuarterDemoSettings {

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
    
    val identifier: Identifier = Open4Identifier(nSamples = 25, nFmps = 15)
    
    val nQuarterIdentifier: NQuarterIdentifier = NQuarterIdentifier(nSamples = 25, nFmps = 15)
}

class NQuarterDemo : Application() {

    override fun start(primaryStage: Stage) {
        val curveControl = DrawingControl(NQuarterDemoSettings.width, NQuarterDemoSettings.height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) {
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    val fsc = NQuarterDemoSettings.generator.generate(it.drawingStroke.inputData)
                    drawPoints(fsc.evaluateAll(0.01))
                    val s = reparametrize(fsc)
                    val primitive = NQuarterDemoSettings.identifier.identify(s)
                    val curve: Curve = when(primitive.curveClass) {
                        CurveClass.OpenFreeCurve -> fsc
                        CurveClass.LineSegment -> primitive.linear.base
                        CurveClass.CircularArc -> {
                            val nQuarter = NQuarterDemoSettings.nQuarterIdentifier.identifyCircular(s)
                            when(nQuarter.nQuarterClass) {
                                NQuarterClass.Quarter1 -> nQuarter.nQuarter1.base
                                NQuarterClass.Quarter2 -> nQuarter.nQuarter2.base
                                NQuarterClass.Quarter3 -> nQuarter.nQuarter3.base
                                NQuarterClass.General -> primitive.circular.base
                            }
                        }
                        CurveClass.EllipticArc -> {
                            val nQuarter = NQuarterDemoSettings.nQuarterIdentifier.identifyElliptic(s)
                            when(nQuarter.nQuarterClass) {
                                NQuarterClass.Quarter1 -> nQuarter.nQuarter1.base
                                NQuarterClass.Quarter2 -> nQuarter.nQuarter2.base
                                NQuarterClass.Quarter3 -> nQuarter.nQuarter3.base
                                NQuarterClass.General -> primitive.elliptic.base
                            }
                        }
                        else -> error("")
                    }
                    when(curve) {
                        is BSpline -> drawCubicBSpline(curve, DrawStyle(Color.MAGENTA))
                        is ConicSection -> {
                            drawConjugateBox(ConjugateBox.ofConicSection(curve), DrawStyle(Color.CYAN))
                            drawConicSection(curve, DrawStyle(Color.MAGENTA))
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