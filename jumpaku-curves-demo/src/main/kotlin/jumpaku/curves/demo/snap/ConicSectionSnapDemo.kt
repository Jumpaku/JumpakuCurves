package jumpaku.curves.demo.snap

import jumpaku.commons.control.None
import jumpaku.commons.control.Option
import jumpaku.commons.control.Some
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.graphics.swing.DrawingPanel
import jumpaku.curves.fsc.DrawingStroke
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
import jumpaku.curves.fsc.snap.point.IFGS
import jumpaku.curves.graphics.*
import java.awt.*
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.pow

fun main() = SwingUtilities.invokeLater {
    val demo = DemoPanel()
    val drawing = DrawingPanel().apply {
        addCurveListener { demo.update(it.drawingStroke) }
        add(demo)
    }
    JFrame("SnapDemo").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        contentPane.add(drawing)
        pack()
        isVisible = true
    }
}


object Settings {

    val width = 640

    val height = 480

    val generator: Generator = Generator(
            degree = 3,
            knotSpan = 0.075,
            fillSpan = 0.0375,
            extendInnerSpan = 0.075,
            extendOuterSpan = 0.075,
            extendDegree = 2,
            fuzzifier = Fuzzifier.Linear(
                    velocityCoefficient = 0.025,
                    accelerationCoefficient = 0.001
            ))

    val identifier: Identifier = Open4Identifier(nSamples = 25, nFmps = 15)

    val baseGrid: Grid = Grid(
            baseSpacing = 64.0,
            baseFuzziness = 8.0,
            magnification = 2,
            origin = Point.xy(width * 0.5, height * 0.5),
            rotation = Rotate(Vector.K, 0.0))

    val snapper: ConicSectionSnapper<*> = ConicSectionSnapper(
            pointSnapper = IFGS,
            featurePointsCombinator = ConjugateCombinator)
}


class DemoPanel : JPanel() {

    init {
        preferredSize = Dimension(Settings.width, Settings.height)
    }

    private val results = mutableListOf<Pair<BSpline, Option<ConicSection>>>()

    fun update(drawingStroke: DrawingStroke) {
        val fsc = Settings.generator.generate(drawingStroke)
        val identified = Settings.identifier.identify(reparametrize(fsc))
        val result = if (identified.curveClass.isConicSection) {
            val cs = when (identified.curveClass) {
                CurveClass.LineSegment -> identified.linear.base
                CurveClass.CircularArc -> identified.circular.base
                CurveClass.EllipticArc -> identified.elliptic.base
                else -> error("")
            }
            val snapped = Settings.snapper.snap(
                    Settings.baseGrid, cs, identified.curveClass).snappedConicSection
            snapped
        } else None
        results += fsc to result
        repaint()
    }

    override fun paint(g: Graphics) = with(g as Graphics2D) {
        results.forEach { (fsc, cs) ->
            drawGrid()
            drawPoints(fsc.evaluateAll(0.01), DrawStyle(Color.LIGHT_GRAY))
            when (cs) {
                None -> drawCubicBSpline(fsc, DrawStyle(Color.MAGENTA))
                is Some -> {
                    drawConjugateBox(ConjugateBox.ofConicSection(cs.value), DrawStyle(Color.CYAN))
                    drawConicSection(cs.value, DrawStyle(Color.MAGENTA))
                }
            }
        }
    }
}

private fun Graphics2D.drawGrid() {
    for (r in listOf(-2, 0, 2)) {
        drawGrid(Settings.baseGrid, r,
                0.0, 0.0, Settings.width.toDouble(), Settings.height.toDouble(),
                DrawStyle(Color.GRAY, BasicStroke(2f.pow(-r))))
    }
}
