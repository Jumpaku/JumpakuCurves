package jumpaku.curves.demo.blend

import jumpaku.commons.control.*
import jumpaku.curves.core.curve.Sampler
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.polyline.LineSegment
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.lerp
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.blend.*
import jumpaku.curves.fsc.blend.Blender.SmallInterval
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.graphics.*
import jumpaku.curves.graphics.swing.DrawingPanel
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
    JFrame("BlendDemo").apply {
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

    val blender: Blender = Blender.derive(
        generator,
        samplingSpan = 0.01,
        blendRate = 0.25,
        overlapThreshold = Grade(1e-5)
    )
}


class DemoPanel : JPanel() {

    init {
        preferredSize = Dimension(Settings.width, Settings.height)
    }

    var s0: Option<BSpline> = none()
    var s1: Option<BSpline> = none()
    var s2: Option<BSpline> = none()

    fun update(drawingStroke: DrawingStroke) {
        val s = Settings.generator.generate(drawingStroke)
        when {
            s0.isEmpty -> s0 = some(s)
            s1.isEmpty -> s1 = some(s)
            else -> {
                s0 = s1
                s1 = some(s)
            }
        }
        repaint()
    }

    override fun paint(g: Graphics) = with(g as Graphics2D) {
        val blender = Settings.blender
        s0.forEach { s ->
            drawCubicBSpline(s, DrawStyle(Color.MAGENTA))
            drawPoints(
                s(Sampler(0.01)).map { it.copy(r = it.r * (1 - blender.overlapThreshold.value)) },
                DrawStyle(Color.MAGENTA)
            )
        }
        s1.forEach { s ->
            drawCubicBSpline(s, DrawStyle(Color.ORANGE))
            drawPoints(
                s(Sampler(0.01)).map { it.copy(r = it.r * (1 - blender.overlapThreshold.value)) },
                DrawStyle(Color.ORANGE)
            )
        }
        if (s0 !is Some || s1 !is Some) return@with
        val existingSampled =
            Blender.SampledCurve(
                s0.orThrow(),
                s0.orThrow().domain.sample(blender.samplingSpan).zipWithNext(::SmallInterval)
            )
        val overlappingSampled =
            Blender.SampledCurve(
                s1.orThrow(),
                s1.orThrow().domain.sample(blender.samplingSpan).zipWithNext(::SmallInterval)
            )
        val overlapState = blender.detector.detect(existingSampled, overlappingSampled)
        val osm = overlapState.osm
        for (i in 0 until osm.rowSize) {
            for (j in 0 until osm.columnSize) {
                fillPoint(
                    Point.xyr(5 + j * 5.0, 120 + i * 5.0, 2.5),
                    DrawStyle(
                        if (osm[i, j] == Grade.FALSE) Color.WHITE
                        else Color.getHSBColor(0.5f, 0.7f, osm[i, j].value.toFloat())
                    )
                )
            }
        }
        val detected = overlapState as? OverlapState.Detected ?: return@with
        detected.middle.forEach { (y, x) ->
            fillPoint(Point.xyr(5 + 5.0 * x, 120 + 5.0 * y, 2.5), DrawStyle(Color.ORANGE))
        }
        detected.front.forEach { (y, x) ->
            fillPoint(Point.xyr(5 + 5.0 * x, 120 + 5.0 * y, 1.0), DrawStyle(Color.MAGENTA))
        }
        detected.back.forEach { (y, x) ->
            fillPoint(Point.xyr(5 + 5.0 * x, 120 + 5.0 * y, 1.0), DrawStyle(Color.MAGENTA))
        }
        blender.parametrizer.resample2(existingSampled, overlappingSampled, overlapState).forEach {
            drawLineSegment(LineSegment(it.existing.point, it.overlapping.point))
            fillPoint(it.existing.point.copy(r = 2.0), DrawStyle(Color.BLUE))
            fillPoint(it.overlapping.point.copy(r = 2.0), DrawStyle(Color.BLUE))
            fillPoint(it.blend().point.copy(r = 2.0), DrawStyle(Color.CYAN))
        }
        blender.parametrizer.resample0(existingSampled, overlappingSampled, overlapState).forEach {
            drawLineSegment(LineSegment(it.existing.point, it.overlapping.point))
            fillPoint(it.existing.point.copy(r = 2.0), DrawStyle(Color.RED))
            fillPoint(it.overlapping.point.copy(r = 2.0), DrawStyle(Color.RED))
            fillPoint(it.blend().point.copy(r = 2.0), DrawStyle(Color.CYAN))
        }
        blender.parametrizer.resample4(existingSampled, overlappingSampled, overlapState).forEach {
            drawLineSegment(LineSegment(it.existing.point, it.overlapping.point))
            fillPoint(it.existing.point.copy(r = 2.0), DrawStyle(Color.RED))
            fillPoint(it.overlapping.point.copy(r = 2.0), DrawStyle(Color.RED))
            fillPoint(it.blend().point.copy(r = 2.0), DrawStyle(Color.CYAN))
        }
        blender.parametrizer.resample1(existingSampled, overlappingSampled, overlapState).forEach {
            drawLineSegment(LineSegment(it.existing.point, it.overlapping.point))
            fillPoint(it.existing.point.copy(r = 2.0), DrawStyle(Color.GREEN))
            fillPoint(it.overlapping.point.copy(r = 2.0), DrawStyle(Color.GREEN))
            fillPoint(it.blend().point.copy(r = 2.0), DrawStyle(Color.CYAN))
        }
        blender.parametrizer.resample3(existingSampled, overlappingSampled, overlapState).forEach {
            drawLineSegment(LineSegment(it.existing.point, it.overlapping.point))
            fillPoint(it.existing.point.copy(r = 2.0), DrawStyle(Color.GREEN))
            fillPoint(it.overlapping.point.copy(r = 2.0), DrawStyle(Color.GREEN))
            fillPoint(it.blend().point.copy(r = 2.0), DrawStyle(Color.CYAN))
        }
        blender.parametrizer.parametrize(existingSampled, overlappingSampled, overlapState).let {
            val b = it.first().param
            val e = it.last().param
            it.let { Blender.weightByKde(it,blender.bandWidth) }.forEach {
                val x = 50.0.lerp((it.param - b) / (e - b), 850.0)
                val a0 = Point.xy(x, 100.0)
                val a1 = Point.xy(x, 100.0 - it.weight * 30)
                drawLineSegment(LineSegment(a0, a1))
            }
        }
        /*
        when (val blended = blender.tryBlend(s0.orThrow(), s1.orThrow())) {
            is BlendResult.NotBlended -> return@with
            is BlendResult.Blended -> {
                drawCubicBSpline(blended.blended, DrawStyle(Color.BLACK))
                drawPoints(
                    blended.blended(Sampler(0.01)).map { it.copy(r = it.r * (1 - blender.overlapThreshold.value)) },
                    DrawStyle(Color.BLACK)
                )
            }
        }
        */
    }
}
