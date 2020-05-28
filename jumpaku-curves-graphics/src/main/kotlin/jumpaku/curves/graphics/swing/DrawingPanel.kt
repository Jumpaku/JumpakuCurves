package jumpaku.curves.graphics.swing

import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.DrawingStroke
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseEvent
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import java.util.*
import javax.swing.JPanel
import javax.swing.OverlayLayout
import javax.swing.event.MouseInputAdapter


class CurveEvent(source: DrawingPanel, val drawingStroke: DrawingStroke)
    : EventObject(source) {
    override fun getSource(): DrawingPanel {
        return super.getSource() as DrawingPanel
    }
}

interface CurveListener : EventListener {
    fun curveDrawn(curveEvent: CurveEvent) {}
}

class DrawingPanel : JPanel() {

    val points = mutableListOf<ParamPoint>()

    val listener = object : MouseInputAdapter() {

        override fun mouseDragged(e: MouseEvent) {
            val point = e.run {
                ParamPoint(Point.xy(x.toDouble(), y.toDouble()), System.nanoTime() * 1e-9)
            }
            synchronized(points) {
                points.add(point)
            }
            repaint()
        }

        override fun mouseReleased(e: MouseEvent) {
            synchronized(points) {
                if (points.size >= 2) {
                    fireCurveEvent(CurveEvent(this@DrawingPanel, DrawingStroke(points)))
                }
                points.clear()
            }
            repaint()
        }
    }

    init {
        layout = OverlayLayout(this)
        isOpaque = false
        addMouseListener(listener)
        addMouseMotionListener(listener)
    }

    fun addCurveListener(listener: CurveListener) {
        listenerList.add(CurveListener::class.java, listener)
    }

    fun addCurveListener(listener: (CurveEvent) -> Unit) {
        addCurveListener(object : CurveListener {
            override fun curveDrawn(curveEvent: CurveEvent) = listener(curveEvent)
        })
    }

    fun removeCurveListener(listener: CurveListener) {
        listenerList.remove(CurveListener::class.java, listener)
    }

    fun fireCurveEvent(curveEvent: CurveEvent) {
        val listeners = listenerList.getListeners(CurveListener::class.java)
        listeners.forEach {
            it.curveDrawn(curveEvent)
        }
    }

    override fun paint(g: Graphics) {
        synchronized(points) {
            points.map { Point2D.Double(it.point.x, it.point.y) }
                    .zipWithNext(Line2D::Double)
                    .forEach((g as Graphics2D)::draw)
        }
        paintChildren(g)
    }
}