package jumpaku.curves.demo.generate

import jumpaku.curves.core.curve.Sampler
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.graphics.swing.DrawingPanel
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.graphics.drawCubicBezier
import jumpaku.curves.graphics.drawPoints
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
    JFrame("GenerateDemo").apply {
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
}


class DemoPanel : JPanel() {

    init {
        preferredSize = Dimension(Settings.width, Settings.height)
    }

    private val results = mutableListOf<BSpline>()

    fun update(drawingStroke: DrawingStroke) {
        val fsc = Settings.generator.generate(drawingStroke)
        results += fsc
        repaint()
    }

    override fun paint(g: Graphics) = with(g as Graphics2D) {
        results.forEach { fsc ->
            fsc.toBeziers().forEach { drawCubicBezier(it) }
            drawPoints(fsc.invoke(Sampler(0.01)))
        }
    }
}
