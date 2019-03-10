package jumpaku.curves.graphics

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Stroke

class DrawStyle(
        val color: Color = Color.BLACK,
        val stroke: Stroke = BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)) : (Graphics2D) -> Unit {

    override fun invoke(g: Graphics2D) {
        g.color = color
        g.stroke = stroke
    }
}

class FillStyle(val color: Color = Color(0, 0, 0, 0)) : (Graphics2D) -> Unit {
    override fun invoke(g: Graphics2D) {
        g.color = color
    }
}