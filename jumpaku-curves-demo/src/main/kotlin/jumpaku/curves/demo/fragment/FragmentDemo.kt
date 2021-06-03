package jumpaku.curves.demo.fragment

import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.graphics.swing.DrawingPanel
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.fragment.Chunk
import jumpaku.curves.fsc.fragment.Fragment
import jumpaku.curves.fsc.fragment.Fragmenter
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.graphics.DrawStyle
import jumpaku.curves.graphics.drawCubicBSpline
import jumpaku.curves.graphics.drawPoints
import java.awt.*
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities


fun main() = SwingUtilities.invokeLater {
    val demo = DemoPanel()
    val drawing = DrawingPanel().apply {
        addCurveListener { demo.update(it.drawingStroke) }
        add(demo)
    }
    JFrame("FragmentDemo").apply {
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
            knotSpan = 0.1,
            fillSpan = 0.025,
            extendInnerSpan = 0.1,
            extendOuterSpan = 0.1,
            extendDegree = 2,
            fuzzifier = Fuzzifier.Linear(
                    velocityCoefficient = 0.008,
                    accelerationCoefficient = 0.007
            ))

    val fragmenter: Fragmenter = Fragmenter(
            threshold = Chunk.Threshold(
                    necessity = 0.37,
                    possibility = 0.8),
            chunkSize = 4,
            minStayTimeSpan = 0.05)
}


class DemoPanel : JPanel() {

    init {
        preferredSize = Dimension(Settings.width, Settings.height)
    }

    private val results = mutableListOf<Pair<BSpline, List<Fragment>>>()

    fun update(drawingStroke: DrawingStroke) {
        val fsc = Settings.generator.generate(drawingStroke)
        val fragments = Settings.fragmenter.fragment(fsc)
        results += fsc to fragments
        repaint()
    }

    override fun paint(g: Graphics) = with(g as Graphics2D) {
        results.forEach { (fsc, fragments) ->
            fragments.filter { it.type == Fragment.Type.Move }.map { fsc.restrict(it.interval) }.apply {
                forEach { drawPoints(it.evaluateAll(0.01)) }
                forEach { drawCubicBSpline(it, DrawStyle(Color.MAGENTA, BasicStroke(3f))) }
            }
        }
    }
}
