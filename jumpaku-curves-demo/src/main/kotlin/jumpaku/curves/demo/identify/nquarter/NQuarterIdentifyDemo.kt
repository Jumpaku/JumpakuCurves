package jumpaku.curves.demo.identify.nquarter

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.graphics.swing.DrawingPanel
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.identify.nquarter.NQuarterClass
import jumpaku.curves.fsc.identify.nquarter.NQuarterIdentifier
import jumpaku.curves.fsc.identify.primitive.*
import jumpaku.curves.fsc.snap.conicsection.ConjugateBox
import jumpaku.curves.graphics.DrawStyle
import jumpaku.curves.graphics.drawConicSection
import jumpaku.curves.graphics.drawConjugateBox
import jumpaku.curves.graphics.drawCubicBSpline
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities


fun main() = SwingUtilities.invokeLater {
    val demo = DemoPanel()
    val drawing = DrawingPanel().apply {
        addCurveListener { demo.update(it.drawingStroke) }
        add(demo)
    }
    JFrame("IdentifyDemo").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        contentPane.add(drawing)
        pack()
        isVisible = true
    }
}


object Settings {

    val width = 1280

    val height = 720

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

    val nQuarterIdentifier: NQuarterIdentifier = NQuarterIdentifier(nSamples = 25, nFmps = 15)
}


class DemoPanel : JPanel() {

    init {
        preferredSize = Dimension(Settings.width, Settings.height)
    }

    private val results = mutableListOf<Curve>()

    fun update(drawingStroke: DrawingStroke) {
        val fsc = Settings.generator.generate(drawingStroke.inputData)
        val primitive = Settings.identifier.identify(reparametrize(fsc))
        val s = reparametrize(fsc)
        val curve: Curve = when (primitive.curveClass) {
            CurveClass.OpenFreeCurve -> fsc
            CurveClass.LineSegment -> primitive.linear.base
            CurveClass.CircularArc -> {
                val nQuarter = Settings.nQuarterIdentifier.identifyCircular(s)
                when (nQuarter.nQuarterClass) {
                    NQuarterClass.Quarter1 -> nQuarter.nQuarter1.base
                    NQuarterClass.Quarter2 -> nQuarter.nQuarter2.base
                    NQuarterClass.Quarter3 -> nQuarter.nQuarter3.base
                    NQuarterClass.General -> primitive.circular.base
                }
            }
            CurveClass.EllipticArc -> {
                val nQuarter = Settings.nQuarterIdentifier.identifyElliptic(s)
                when (nQuarter.nQuarterClass) {
                    NQuarterClass.Quarter1 -> nQuarter.nQuarter1.base
                    NQuarterClass.Quarter2 -> nQuarter.nQuarter2.base
                    NQuarterClass.Quarter3 -> nQuarter.nQuarter3.base
                    NQuarterClass.General -> primitive.elliptic.base
                }
            }
            else -> error("")
        }
        results += curve
        repaint()
    }

    override fun paint(g: Graphics) = with(g as Graphics2D) {
        results.forEach { curve ->
            when (curve) {
                is BSpline -> drawCubicBSpline(curve, DrawStyle(Color.MAGENTA))
                is ConicSection -> {
                    drawConjugateBox(ConjugateBox.ofConicSection(curve), DrawStyle(Color.CYAN))
                    drawConicSection(curve, DrawStyle(Color.MAGENTA))
                }
            }
        }
    }
}
