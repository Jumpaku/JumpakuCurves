package jumpaku.curves.demo.identify.primitive.multi

import jumpaku.commons.control.Option
import jumpaku.commons.control.Some
import jumpaku.commons.control.orDefault
import jumpaku.commons.control.toOption
import jumpaku.commons.math.tryDiv
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.curve.transformParams
import jumpaku.curves.core.geom.Line
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.line
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.identify.primitive.multireference.CircularGenerator3
import jumpaku.curves.fsc.identify.primitive.multireference.EllipticGenerator2
import jumpaku.curves.fsc.identify.primitive.multireference.EllipticGenerator3
import jumpaku.curves.fsc.identify.primitive.multireference.LinearGenerator3
import jumpaku.curves.fsc.identify.primitive.reparametrize
import jumpaku.curves.graphics.drawCubicBSpline
import jumpaku.curves.graphics.drawPoints
import jumpaku.curves.graphics.drawPolyline
import jumpaku.curves.graphics.swing.DrawingPanel
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
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

object Settings {
    val generator = Generator()
    val generations = 2
    val linear = LinearGenerator3(generations)
    val circular = CircularGenerator3(generations)
    val elliptic = EllipticGenerator3(generations)
}

class DemoPanel : JPanel() {

    init {
        preferredSize = Dimension(640, 480)
        //isFocusable = true
        /*addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val p0 = Point.xy(100.0, 300.0)
                val p1 = Point.xy(300.0, 400.0)
                val p2 = Point.xy(500.0, 300.0)
                val w = computeWeight(p0, p1, p2, Point.xy(e.x.toDouble(), e.y.toDouble()))
                println(w)
            }
        })*/
    }

    var fsc: Option<BSpline> = ConicSection(
            Point.xy(100.0, 100.0),
            Point.xy(300.0, 300.0),
            Point.xy(500.0, 100.0), 0.0).toOption().map {
        reparametrize(it).sample(1000)
                .let { transformParams(it, Interval(0.0, 1.0), Interval(5.0, 15.0)) }
                .let { DrawingStroke(it) }
                .let { Settings.generator.generate(it) }
    }//None

    var updated = true
    fun update(drawingStroke: DrawingStroke) {
        fsc = Some(Settings.generator.generate(drawingStroke))
        updated = true
        repaint()
    }

    override fun paint(g: Graphics) = if (!updated) Unit else with(g as Graphics2D) {
        updated = false
        val s = fsc.orNull() ?: return@with
        drawCubicBSpline(s)
        drawPoints(reparametrize(s).evaluateAll(0.01))
        val reparametrized = reparametrize(s)
        val m = Settings.elliptic.generate(reparametrized)
        val generators = listOf(
                EllipticGenerator2(3),
                EllipticGenerator3(3)
        )
        generators.forEach {generator->
            val ref = generator.generate(reparametrized)
            m.elements.forEachIndexed { i, r ->
                //drawPolyline(Polyline.byIndices(r.bezier.representPoints.let { it + it.first() }))
            }
        }
        val n = 1000
        val ps = m.evaluateAll(n)
        val q = ps.map { p->
            p.points.map { it.run { Area(Ellipse2D.Double(x - r / 2, y - r / 2, r, r)) } }.reduce { acc, a -> acc.intersect(a); acc }
        }.reduce { acc, area -> acc.add(area); acc }

        draw(q)

        m.elements.indices.forEach { i ->
            //drawPolyline(Polyline.byIndices(ps.map { it[i] }))
        }
        println(ps.map { it.vertex().map { v -> it.isPossible(v).value }.orDefault { 0.0 } }.min())
    }
}

fun computeWeight(p0: Point, p1: Point, p2: Point, p: Point): Double {
    val a = p1.projectTo(line(p, p + (p2 - p0)).orThrow())
    val b = p1.projectTo(Line(p0, p2))
    val t = (a - p1).dot(b - p1).tryDiv(b.distSquare(p1)).orThrow()//.value().orDefault(0.0)
    val x = p1.lerp(t, p0.middle(p2))
    val dd = x.distSquare(p)
    val ll = p0.distSquare(p2) / 4
    val yi = dd + t * t * ll - 2 * t * ll
    val xi = ll * t * t - dd
    return yi / xi
}
