package jumpaku.curves.demo

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve2
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve_old
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.transform.Translate
import jumpaku.curves.core.transform.UniformlyScale
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.graphics.DrawStyle
import jumpaku.curves.graphics.drawConicSection
import jumpaku.curves.graphics.drawPoints
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.ui.RectangleInsets
import org.jfree.data.xy.XYDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.cos
import kotlin.math.sin


fun createDataset(curve: Curve): XYDataset {
    val series = XYSeries("Truth").apply {
        val ts = curve.domain.sample(1000000)
        val ps = curve(ts)
        val ds = ps.zipWithNext(Point::dist).scan(0.0, Double::plus)
        val l = ds.last()
        ts.zip(ds).forEach { (t, d) ->
            add(t, d / l)
        }
    }
    val tsFirstHalf = listOf(
        0.0,
        0.03125,
        0.0625,
        0.09375,
        0.125,
        0.15625,
        0.1875,
        0.21875,
        0.25,
        0.28125,
        0.3125,
        0.34375,
        0.344891003751036,
        0.375,
        0.40625,
        0.408463201989395,
        0.435292094467113,
        0.4375,
        0.450144971866358,
        0.459613771515324,
        0.466202963249812,
        0.46875,
        0.471072560016151,
        0.474833591545413,
        0.477838553667494,
        0.480305104250766,
        0.482374944944359,
        0.484144328328536,
        0.485680957274281,
        0.487033868300738,
        0.488239477513599,
        0.489325422160593,
        0.490313082192825,
        0.491219282607574,
        0.492057471539545,
        0.492838553746554,
        0.493571492050083,
        0.49426374917309,
        0.494921617963868,
        0.495550471979488,
        0.496154958771083,
        0.496739151249323,
        0.497306668094005,
        0.497860771361831,
        0.498404447114394,
        0.498940473523741,
        0.499471479993633,
    )
    val samplingParams = listOf(tsFirstHalf, listOf(0.5), tsFirstHalf.asReversed().map { 1.0 - it }).flatten()
    val series1 = XYSeries("Old").apply {
        val ls = Interval.Unit.sample(15)
        val r = ReparametrizedCurve_old.of(curve, samplingParams)
        val ts = ls.map { r.reparametrizer.toOriginal(it) }
        ts.zip(ls).forEach { (t, l) ->
            add(t, l)
        }
    }
    val series2 = XYSeries("LinearFit").apply {
        val ls = Interval.Unit.sample(15)
        val r = ReparametrizedCurve2.of(curve, samplingParams)
        val ts = r.toOriginal(ls)
        ts.zip(ls).forEach { (t, l) ->
            add(t, l)
        }
    }
    val series3 = XYSeries("LinearFitMore").apply {
        val ls = Interval.Unit.sample(15)
        val r = ReparametrizedCurve2.of(curve, samplingParams)
        val ts = r.toOriginal(ls)
        ts.zip(ls).forEach { (t, l) ->
            add(t, l)
        }
    }
    return XYSeriesCollection().apply {
        addSeries(series)
        //addSeries(series1)
        addSeries(series2)
        //addSeries(series3)
    }
}

fun createChart(dataset: XYDataset): JFreeChart = ChartFactory.createXYLineChart(
    "arc length",  // title
    "parameter",  // x-axis label
    "arc-length ratio",  // y-axis label
    dataset
).apply {
    backgroundPaint = Color.WHITE
    with(plot as XYPlot) {
        backgroundPaint = Color.LIGHT_GRAY
        domainGridlinePaint = Color.WHITE
        rangeGridlinePaint = Color.WHITE
        axisOffset = RectangleInsets(5.0, 5.0, 5.0, 5.0)
        isDomainCrosshairVisible = true
        isRangeCrosshairVisible = true

        val r = renderer as XYLineAndShapeRenderer
        r.defaultShapesVisible = true
        r.defaultShapesFilled = true
        r.drawSeriesLineAsPath = true
        r.defaultShapesVisible = false
    }
}

fun main() = SwingUtilities.invokeLater {

    val angle = Math.toRadians(358.0)
    val w = cos(0.5 * angle)
    val curve = ConicSection(
        Point.xy(cos(angle / 2), sin(angle / 2)),
        Point.xy(1.0, 0.0),
        Point.xy(cos(-angle / 2), sin(-angle / 2)),
        w
    ).transform(UniformlyScale(100.0).andThen(Translate(100.0, 100.0)))

    val chart: JFreeChart = createChart(createDataset(curve))
    val panel = ChartPanel(chart, false)//TestPanel(curve)//
    panel.fillZoomRectangle = true
    JFrame("DemoTemplate").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        contentPane.add(panel)
        pack()
        isVisible = true
    }
}


class TestPanel(val curve: Curve) : JPanel() {

    init {
        preferredSize = Dimension(1280, 720)
    }

    private val drawingStrokes = mutableListOf<DrawingStroke>()

    fun update(drawingStroke: DrawingStroke) {
        drawingStrokes += drawingStroke
        repaint()
    }

    val samplingParams = listOf(
        0.0,
        0.03125,
        0.0625,
        0.09375,
        0.125,
        0.15625,
        0.1875,
        0.21875,
        0.25,
        0.28125,
        0.3125,
        0.34375,
        0.344891003751036,
        0.375,
        0.40625,
        0.408463201989395,
        0.435292094467113,
        0.4375,
        0.450144971866358,
        0.459613771515324,
        0.466202963249812,
        0.46875,
        0.471072560016151,
        0.474833591545413,
        0.477838553667494,
        0.480305104250766,
        0.482374944944359,
        0.484144328328536,
        0.485680957274281,
        0.487033868300738,
        0.488239477513599,
        0.489325422160593,
        0.490313082192825,
        0.491219282607574,
        0.492057471539545,
        0.492838553746554,
        0.493571492050083,
        0.49426374917309,
        0.494921617963868,
        0.495550471979488,
        0.496154958771083,
        0.496739151249323,
        0.497306668094005,
        0.497860771361831,
        0.498404447114394,
        0.498940473523741,
        0.499471479993633,
    ).let { tsFirstHalf -> listOf(tsFirstHalf, listOf(0.5), tsFirstHalf.asReversed().map { 1.0 - it }).flatten() }

    override fun paint(g: Graphics) {
        val g2d = g as Graphics2D
        g2d.drawConicSection(curve as ConicSection)
        Unit.apply {
            val ls = Interval.Unit.sample(15)
            val r = ReparametrizedCurve2.of(curve, curve.domain.sample(1000000))
            val ts = ls.map { r.toOriginal(it) }
            g2d.drawPoints(curve(ts).map { it.copy(r=2.0) })
            Interval.Unit.sample(65).map { r.toOriginal(it) }.map(::println)
        }
        Unit.apply {
            val ls = Interval.Unit.sample(15)
            val r = ReparametrizedCurve_old.of(curve, samplingParams)
            val ts = ls.map { r.reparametrizer.toOriginal(it) }
            g2d.drawPoints(curve(ts).map { it.copy(r=3.0) },DrawStyle(Color.MAGENTA))
        }
        Unit.apply {
            val ls = Interval.Unit.sample(15)
            val r = ReparametrizedCurve2.of(curve, samplingParams)
            val ts = r.toOriginal(ls)
            g2d.drawPoints(curve(ts).map { it.copy(r=4.0) },DrawStyle(Color.GREEN))
        }
        Unit.apply {
            val ls = Interval.Unit.sample(15)
            val r = ReparametrizedCurve2.of(curve, samplingParams)
            val ts = r.toOriginal(ls)
            g2d.drawPoints(curve(ts).map { it.copy(r=5.0) },DrawStyle(Color.BLUE))
        }
    }
}
