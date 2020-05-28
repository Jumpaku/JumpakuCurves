package jumpaku.curves.demo.freecurve

import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.graphics.swing.DrawingPanel
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.freecurve.*
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.identify.primitive.Open4Identifier
import jumpaku.curves.graphics.*
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
    JFrame("ShapeDemo").apply {
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

    val shaper: Shaper = Shaper(
            segmenter = Segmenter(Open4Identifier(
                    nSamples = 25,
                    nFmps = 15)),
            smoother = Smoother(
                    pruningFactor = 2.0,
                    samplingFactor = 33),
            sampler = Shaper.Sampler.ByFixedNumber(100))
}


class DemoPanel : JPanel() {

    init {
        preferredSize = Dimension(Settings.width, Settings.height)
    }

    private val results = mutableListOf<Pair<BSpline, Triple<List<Double>, SegmentResult, SmoothResult>>>()

    fun update(drawingStroke: DrawingStroke) {
        val fsc = Settings.generator.generate(drawingStroke)
        val result = Settings.shaper.shape(fsc)
        results += fsc to result
        repaint()
    }

    override fun paint(g: Graphics) = with(g as Graphics2D) {
        results.forEach { (fsc, result) ->
            val (_, _, smooth) = result
            drawPoints(fsc.evaluateAll(0.01))
            smooth.conicSections.forEach { drawConicSection(it, DrawStyle(Color.MAGENTA)) }
            smooth.cubicBeziers.forEach { drawCubicBezier(it, DrawStyle(Color.CYAN)) }
        }
    }
}
