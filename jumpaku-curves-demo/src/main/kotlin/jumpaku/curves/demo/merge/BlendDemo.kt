package jumpaku.curves.demo.merge

import jumpaku.commons.control.Option
import jumpaku.commons.control.none
import jumpaku.commons.control.or
import jumpaku.commons.control.some
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.demo.DrawingPanel
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.merge.Merger
import jumpaku.curves.graphics.DrawStyle
import jumpaku.curves.graphics.drawCubicBSpline
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
    JFrame("MergeDemo").apply {
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
            degree = 4,
            knotSpan = 0.1,
            fillSpan = 0.1 / 3,
            extendInnerSpan = 0.15,
            extendOuterSpan = 0.1,
            extendDegree = 2,
            fuzzifier = Fuzzifier.Linear(
                    velocityCoefficient = 0.008,
                    accelerationCoefficient = 0.007
            ))

    val merger: Merger = Merger.derive(generator,
            samplingSpan = 0.01,
            mergeRate = 0.5,
            overlapThreshold = Grade(1e-10))
}


class DemoPanel : JPanel() {

    init {
        preferredSize = Dimension(Settings.width, Settings.height)
    }

    var existingFsc: Option<BSpline> = none()

    fun update(drawingStroke: DrawingStroke) {
        val o = Settings.generator.generate(drawingStroke)
        existingFsc.ifPresent { s ->
            existingFsc = Settings.merger.tryMerge(s, o).or(existingFsc)
        }.ifAbsent {
            existingFsc = some(o)
        }
        repaint()
    }

    override fun paint(g: Graphics) = with(g as Graphics2D) {
        existingFsc.forEach { s ->
            drawCubicBSpline(s, DrawStyle())
            drawPoints(s.evaluateAll(0.01), DrawStyle())
        }
    }
}
