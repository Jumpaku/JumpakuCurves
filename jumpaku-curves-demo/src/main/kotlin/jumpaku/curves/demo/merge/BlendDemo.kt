package jumpaku.curves.demo.merge

import jumpaku.commons.control.Option
import jumpaku.commons.control.none
import jumpaku.commons.control.or
import jumpaku.commons.control.some
import jumpaku.curves.core.curve.Sampler
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.graphics.swing.DrawingPanel
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.blend.BlendResult
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.merge.Merger
import jumpaku.curves.graphics.DrawStyle
import jumpaku.curves.graphics.drawCubicBSpline
import jumpaku.curves.graphics.drawPoints
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities


fun main() = SwingUtilities.invokeLater {
    val demo = ComparePanel()
    JFrame("MergeDemo").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        contentPane.add(demo)
        pack()
        isVisible = true
    }
}


object Settings {

    val width = 1280

    val height = 720

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
        )
    )

    val merger: Merger = Merger.derive(
        generator,
        samplingSpan = 0.005,
        mergeRate = 0.5,
        overlapThreshold = Grade(0.5)
    )
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
            drawPoints(s.invoke(Sampler(0.01)), DrawStyle())
        }
    }
}

class ComparePanel : JPanel() {

    class FscsView : JPanel() {
        val fscs: MutableList<BSpline> = mutableListOf()

        init {
            preferredSize = Dimension(Settings.width / 3, Settings.height)
        }

        override fun paint(g: Graphics) {
            (g as? Graphics2D)?.apply {
                fscs.map {
                    drawCubicBSpline(it)
                    drawPoints(it(Sampler(0.01)))
                }
                drawRect(0, 0, preferredSize.width, preferredSize.height)
            }

        }
    }

    var fsc: BSpline? = null
    val oldView = FscsView()
    val newView = FscsView()
    val inputView = FscsView()
    val drawing = DrawingPanel().apply {
        addCurveListener {
            if (fsc == null) {
                val s = Settings.generator.generate(it.drawingStroke)
                fsc = s
                inputView.apply {
                    fscs.clear()
                    fscs.add(fsc!!)
                    repaint()
                }
                return@addCurveListener
            }
            val s0 = fsc!!
            val s1 = Settings.generator.generate(it.drawingStroke)
            oldView.apply {
                val fsc = Settings.merger.tryMerge(s0, s1).orNull() ?: return@apply
                fscs.clear()
                fscs.add(fsc)
                repaint()
            }
            newView.apply {
                val fsc = jumpaku.curves.demo.blend.Settings.blender.tryBlend(s0, s1) as? BlendResult.Blended
                    ?: return@apply
                fscs.clear()
                fscs.add(fsc.blended)
                repaint()
            }
            inputView.apply {
                fscs.clear()
                fscs.add(s0)
                fscs.add(s1)
                repaint()
            }
        }
        add(inputView)
    }

    init {
        preferredSize = Dimension(Settings.width, Settings.height)
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        add(oldView)
        add(newView)
        add(drawing)
    }
}
