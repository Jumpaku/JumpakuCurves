package jumpaku.curves.demo.merge

import jumpaku.commons.control.*
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.lerp
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.generate.fit.weighted
import jumpaku.curves.fsc.merge.*
import jumpaku.curves.graphics.DrawStyle
import jumpaku.curves.graphics.drawCubicBSpline
import jumpaku.curves.graphics.drawPoint
import jumpaku.curves.graphics.drawPoints
import jumpaku.curves.graphics.swing.DrawingPanel
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities

fun main() = SwingUtilities.invokeLater {
    val demo = ComparePanel()
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


object CompareSettings {

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
            ))

    val merger: Merger = Merger.derive(generator,
            samplingSpan = 0.01,
            mergeRate = 0.75,
            overlapThreshold = Grade(0.5))

    val merger2: Merger2 = Merger2.derive(generator,
            samplingSpan = 0.01,
            mergeRate = 0.75,
            overlapThreshold = Grade(0.5))
}


class ComparePanel : JPanel() {

    init {
        preferredSize = Dimension(CompareSettings.width, CompareSettings.height)
    }

    var existFsc: Option<BSpline> = none()
    var overlapFsc: Option<BSpline> = none()

    fun update(drawingStroke: DrawingStroke) {
        println(drawingStroke.run { inputData.size / paramSpan })
        overlapFsc = Some(CompareSettings.generator.generate(drawingStroke))
        repaint()
    }

    override fun paint(g: Graphics) = with(g as Graphics2D) {
        existFsc.forEach { e ->
            val eKnotVector = e.knotVector.run {
                KnotVector(degree, knots.map { it.copy(value = it.value - domain.begin) })
            }
            overlapFsc.forEach { o ->
                val oKnotVector = o.knotVector.run {
                    KnotVector(degree, knots.map { it.copy(value = it.value - domain.begin) })
                }

                experiment(BSpline(e.controlPoints, eKnotVector), BSpline(o.controlPoints, oKnotVector))
            }
        }
        //overlapFsc.forEach { s ->
        //    drawCubicBSpline(s, DrawStyle())
        //    drawPoints(s.evaluateAll(0.01), DrawStyle(color = Color.RED))
        //}
        existFsc = existFsc.flatMap { e ->
            overlapFsc.flatMap { o -> CompareSettings.merger.tryMerge(e, o) }.or(existFsc)
        }.or(overlapFsc)
        existFsc.forEach { s ->
            drawCubicBSpline(s, DrawStyle())
            drawPoints(s.evaluateAll(0.01), DrawStyle(color = Color.BLACK))
        }
        overlapFsc = None

    }

    fun Graphics2D.experiment(e: BSpline, o: BSpline) {

        CompareSettings.merger2.run {
            val fsc0 = existFsc.orNull() ?: return@run
            val fsc1 = overlapFsc.orNull() ?: return@run

            val samples0 = fsc0.sample(samplingSpan)
            val samples1 = fsc1.sample(samplingSpan)

            val state = when (val found = detector.detect(samples0, samples1)) {
                is OverlapState2.NotFound -> return@run
                else -> found as OverlapState2.Found
            }
            drawOsm(state.osm)
            drawRidge(state.coreRidge)

            val (overlapBegin0, overlapBegin1) = state.coreRidge.first()
            val (overlapEnd0, overlapEnd1) = state.coreRidge.last()
            val (transitionBegin0, transitionBegin1) = state.transitionBegin
            val (transitionEnd0, transitionEnd1) = state.transitionEnd
            drawOsmPoint(state.coreRidge.first(), Color.MAGENTA)
            drawOsmPoint(state.coreRidge.last(), Color.MAGENTA)
            drawOsmPoint(state.transitionBegin, Color.CYAN)
            drawOsmPoint(state.transitionEnd, Color.CYAN)

            val segmentation0 = DomainSegmentation.segment(fsc0,
                    samples0[transitionBegin0].param,
                    samples0[transitionEnd0].param,
                    samples0[overlapBegin0].param,
                    samples0[overlapEnd0].param
            )
            val segmentation1 = DomainSegmentation.segment(fsc1,
                    samples1[transitionBegin1].param,
                    samples1[transitionEnd1].param,
                    samples1[overlapBegin1].param,
                    samples1[overlapEnd1].param
            )

            listOf(segmentation0.remainFront, segmentation0.remainBack).flatMap { it.sample(samplingSpan).map(fsc0) }
                    .forEach { drawPoint(it.copy(r = 2.0), DrawStyle(Color.BLUE)) }
            listOf(segmentation0.transitionFront, segmentation0.transitionBack).flatMap { it.sample(samplingSpan).map(fsc0) }
                    .forEach { drawPoint(it.copy(r = 2.0), DrawStyle(Color.GREEN)) }
            segmentation0.overlap.sample(samplingSpan).map(fsc0)
                    .forEach { drawPoint(it.copy(r = 2.0), DrawStyle(Color.RED)) }

            listOf(segmentation1.remainFront, segmentation1.remainBack).flatMap { it.sample(samplingSpan).map(fsc1) }
                    .forEach { drawPoint(it.copy(r = 2.0), DrawStyle(Color.BLUE)) }
            listOf(segmentation1.transitionFront, segmentation1.transitionBack).flatMap { it.sample(samplingSpan).map(fsc1) }
                    .forEach { drawPoint(it.copy(r = 2.0), DrawStyle(Color.GREEN)) }
            segmentation1.overlap.sample(samplingSpan).map(fsc1)
                    .forEach { drawPoint(it.copy(r = 2.0), DrawStyle(Color.RED)) }

            val mergeData = resampleMergeData(state.coreRidge, fsc0, fsc1)
            mergeData.forEach { drawPoint(it.point.copy(r = 2.0), DrawStyle(Color.CYAN)) }

            val transition0 = resampleTransitionData(fsc0, segmentation0, mergeData)
            val transition1 = resampleTransitionData(fsc1, segmentation1, mergeData)
            transition0.forEach { drawPoint(it.point.copy(r = 2.0), DrawStyle(Color.MAGENTA)) }
            transition1.forEach { drawPoint(it.point.copy(r = 2.0), DrawStyle(Color.MAGENTA)) }

            val transition = (transition0 + transition1 + mergeData).sortedBy { it.param }
            val remain0 = resampleRemainData(fsc0, segmentation0, transition)
            val remain1 = resampleRemainData(fsc1, segmentation1, transition)
            remain0.forEach { drawPoint(it.point.copy(r = 2.0), DrawStyle(Color.ORANGE)) }
            remain1.forEach { drawPoint(it.point.copy(r = 2.0), DrawStyle(Color.ORANGE)) }

            val data = resample(state.coreRidge, fsc0, segmentation0, fsc1, segmentation1)
            val weighted = weightData(data)
            val x0 = weighted.first().param
            val x1 = weighted.last().param
            weighted.forEach {
                val p = Point.xyr(20.0.lerp((it.param - x0) / (x1 - x0), 520.0), it.point.y, 1.0)
                val sty = DrawStyle(color = Color.getHSBColor(10f, 1f, (it.weight/weighted.map { it.weight }.max()!!).toFloat()))
                drawPoint(p, sty)
            }
            val y0 = weighted.first().param
            val y1 = weighted.last().param
            weighted.forEach {
                val p = Point.xyr(it.point.x, 20.0.lerp((it.param - y0) / (y1 - y0), 520.0), 1.0)
                val sty = DrawStyle(color = Color.getHSBColor(20f, 1f, (it.weight/weighted.map { it.weight }.max()!!).toFloat()))
                drawPoint(p, sty)
            }

        }


        //drawCubicBSpline(e, DrawStyle(color = Color.BLUE))
        //drawPoints(e.evaluateAll(0.01), DrawStyle(color = Color.BLUE))
        //drawCubicBSpline(o, DrawStyle(color = Color.GREEN))
        //drawPoints(o.evaluateAll(0.01), DrawStyle(color = Color.GREEN))
        CompareSettings.merger2.tryMerge(e, o).forEach { update2 ->
            drawCubicBSpline(update2, DrawStyle(color = Color.RED))
            drawPoints(update2.evaluateAll(0.01), DrawStyle(color = Color.RED))
        }

    }

    fun Graphics2D.drawOsmPoint(p: Pair<Int, Int>, color: Color) {
        val pixel = 4
        val (i, j) = p
        this.color = color
        fillRect(j * pixel, i * pixel, pixel, pixel)
    }

    fun Graphics2D.drawOsm(osm: OverlapMatrix) {
        for (i in 0..osm.rowLastIndex) {
            for (j in 0..osm.columnLastIndex) {
                val uij = osm[i, j].value
                drawOsmPoint(i to j, Color.getHSBColor(0f, 0f, 1 - uij.toFloat()))
            }
        }
    }


    fun Graphics2D.drawRidge(ridge: OverlapRidge) {
        ridge.ridge.forEach { (i, j) ->
            val h = 360 * ridge.grade.value.toFloat()
            drawOsmPoint(i to j, Color.getHSBColor(h, 1f, 1f))
        }
    }
}
