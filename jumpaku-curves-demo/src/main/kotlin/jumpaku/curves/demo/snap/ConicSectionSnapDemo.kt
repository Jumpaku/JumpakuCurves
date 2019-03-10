package jumpaku.curves.demo.snap

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.WindowEvent
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.fsc.generate.DataPreparer
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.identify.primitive.CurveClass
import jumpaku.curves.fsc.identify.primitive.Identifier
import jumpaku.curves.fsc.identify.primitive.Open4Identifier
import jumpaku.curves.fsc.identify.primitive.reparametrize
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.conicsection.ConicSectionSnapper
import jumpaku.curves.fsc.snap.conicsection.ConjugateBox
import jumpaku.curves.fsc.snap.conicsection.ConjugateCombinator
import jumpaku.curves.fsc.snap.point.MFGS
import jumpaku.curves.graphics.*
import jumpaku.curves.graphics.fx.DrawingControl
import jumpaku.curves.graphics.fx.DrawingEvent
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import kotlin.math.pow


fun main(vararg args: String) = Application.launch(SnapDemo::class.java, *args)

object SnapDemoSettings {

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

    val baseGrid: Grid = Grid(
            baseSpacing = 64.0,
            baseFuzziness = 8.0,
            magnification = 2,
            origin = Point.xy(width/2, height/2),
            rotation = Rotate(Vector.K, 0.0))

    val snapper: ConicSectionSnapper = ConicSectionSnapper(
            pointSnapper = MFGS(
                    minResolution = -5,
                    maxResolution = 6),
            featurePointsCombinator = ConjugateCombinator())
}

class SnapDemo : Application() {

    override fun start(primaryStage: Stage) {
        val curveControl = DrawingControl(SnapDemoSettings.width, SnapDemoSettings.height).apply {
            addEventHandler(DrawingEvent.DRAWING_DONE) {
                updateGraphics2D {
                    clearRect(0.0, 0.0, width, height)
                    drawGrid()
                    val fsc = SnapDemoSettings.generator.generate(it.drawingStroke.inputData)
                    drawPoints(fsc.evaluateAll(0.01), DrawStyle(Color.LIGHT_GRAY))
                    val identified = SnapDemoSettings.identifier.identify(reparametrize(fsc))
                    when (identified.curveClass) {
                        CurveClass.OpenFreeCurve -> drawCubicBSpline(fsc, DrawStyle(Color.MAGENTA))
                        else -> {
                            val cs = when (identified.curveClass) {
                                CurveClass.LineSegment -> identified.linear.base
                                CurveClass.CircularArc -> identified.circular.base
                                CurveClass.EllipticArc -> identified.elliptic.base
                                else -> error("")
                            }
                            val snapped = SnapDemoSettings.snapper.snap(
                                    SnapDemoSettings.baseGrid, cs, identified.curveClass).snappedConicSection
                            snapped.forEach {
                                drawConjugateBox(ConjugateBox.ofConicSection(it), DrawStyle(Color.CYAN))
                                drawConicSection(it, DrawStyle(Color.MAGENTA))
                            }
                        }
                    }
                }
            }
        }
        primaryStage.apply {
            scene = Scene(curveControl)
            addEventHandler(WindowEvent.WINDOW_SHOWN) {
                curveControl.updateGraphics2D { drawGrid() }
            }
            show()
        }
    }

    fun Graphics2D.drawGrid() {
        for (r in listOf(-2, 0, 2)) {
            drawGrid(SnapDemoSettings.baseGrid, r,
                    0.0, 0.0, SnapDemoSettings.width, SnapDemoSettings.height,
                    DrawStyle(Color.GRAY, BasicStroke(2f.pow(-r))))
        }
    }
}