package jumpaku.curves.demo

import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.graphics.drawPolyline
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
    JFrame("DemoTemplate").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        contentPane.add(drawing)
        pack()
        isVisible = true
    }
}


class DemoPanel : JPanel() {

    init {
        preferredSize = Dimension(640, 480)
    }

    private val drawingStrokes = mutableListOf<DrawingStroke>()

    fun update(drawingStroke: DrawingStroke) {
        drawingStrokes += drawingStroke
        repaint()
    }

    override fun paint(g: Graphics) {
        val g2d = g as Graphics2D
        drawingStrokes.forEach {
            g2d.drawPolyline(Polyline(it.inputData)) {
                it.color = Color.RED
            }
        }
    }
}
