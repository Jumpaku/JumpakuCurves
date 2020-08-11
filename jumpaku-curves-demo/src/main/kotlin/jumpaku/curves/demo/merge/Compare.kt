package jumpaku.curves.demo.merge

import jumpaku.commons.control.*
import jumpaku.commons.math.divOrDefault
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.lerp
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.generate.fit.weighted
import jumpaku.curves.fsc.merge.*
import jumpaku.curves.graphics.DrawStyle
import jumpaku.curves.graphics.drawCubicBSpline
import jumpaku.curves.graphics.drawPoints
import jumpaku.curves.graphics.swing.DrawingPanel
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.max

/*
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
            mergeRate = 0.5,
            overlapThreshold = Grade(1e-10))

    val merger2: Merger2 = Merger2.derive(generator,
            samplingSpan = 0.01,
            mergeRate = 0.5,
            overlapThreshold = Grade(1e-10))
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
        existFsc.forEach { e -> overlapFsc.forEach { o -> experiment(e, o) } }
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
        val samplingSpan = 0.01
        val mergeRate = 0.7
        val eSamples = e.sample(samplingSpan)
        val oSamples = o.sample(samplingSpan)
        println("OK")
        val osm = OverlapMatrix.create(eSamples.map { it.point }, oSamples.map { it.point })
        val pixel = 4
        for (i in eSamples.indices) {
            for (j in oSamples.indices) {
                val uij = osm[i, j].value
                color = Color.getHSBColor(0f, 0f, 1 - uij.toFloat())
                fillRect(j * pixel, i * pixel, pixel, pixel)
            }
        }
        val overlapState = OverlapDetector(Grade(0.0)).detect(eSamples, oSamples).orNull() ?: return
        overlapState.range.forEach { (i, j) ->
            color = Color(1f, 0f, 0f, 0.1f)
            fillRect(j * pixel, i * pixel, pixel, pixel)
        }
        overlapState.ridge.forEach { (i, j) ->
            color = Color(1f, 0f, 0f)
            fillRect(j * pixel, i * pixel, pixel, pixel)
        }

        val rowLast = eSamples.lastIndex
        val columnLast = oSamples.lastIndex
        val range = overlapState.range
        val ridge = overlapState.ridge
        val (eMergeIdxBegin, oMergeIdxBegin) = ridge.first()
        val (eMergeIdxEnd, oMergeIdxEnd) = ridge.last()
        val eFrontRemainIdx = (0 until eMergeIdxBegin).lastOrNull { i -> (i to 0) !in range } ?: 0
        val oFrontRemainIdx = (0 until oMergeIdxBegin).lastOrNull { j -> (0 to j) !in range } ?: 0
        val eBackRemainIdx = (rowLast downTo (eMergeIdxEnd + 1)).lastOrNull { i -> (i to columnLast) !in range }
                ?: rowLast
        val oBackRemainIdx = (columnLast downTo (oMergeIdxEnd + 1)).lastOrNull { j -> (rowLast to j) !in range }
                ?: columnLast
        listOf(eFrontRemainIdx to 0, eBackRemainIdx to columnLast, 0 to oFrontRemainIdx, rowLast to oBackRemainIdx).forEach { (i, j) ->
            color = Color(0f, 1f, 0f)
            fillOval(j * pixel + 1, i * pixel + 1, pixel - 2, pixel - 2)
        }


        val eT0 = eSamples[0].param
        val eT1 = eSamples[eFrontRemainIdx].param
        val eT2 = eSamples[eMergeIdxBegin].param
        val eT3 = eSamples[eMergeIdxEnd].param
        val eT4 = eSamples[eBackRemainIdx].param
        val eT5 = eSamples[rowLast].param
        val oT0 = oSamples[0].param
        val oT1 = oSamples[oFrontRemainIdx].param
        val oT2 = oSamples[oMergeIdxBegin].param
        val oT3 = oSamples[oMergeIdxEnd].param
        val oT4 = oSamples[oBackRemainIdx].param
        val oT5 = oSamples[columnLast].param

        val s2 = eT2.lerp(mergeRate, oT2)
        val s3 = eT3.lerp(mergeRate, oT3)


        val eTransform = TransformParam(s2, s3, eT0, eT1, eT2, eT3, eT4, eT5)
        val eData = eSamples.map { it.copy(param = eTransform(it.param)) }

        val oTransform = TransformParam(s2, s3, oT0, oT1, oT2, oT3, oT4, oT5)
        val oData = oSamples.map { it.copy(param = oTransform(it.param)) }

        val span = max(e.domain.span, o.domain.span)
        eSamples.indices.forEach { index ->
            val param0 = 0.0.lerp((eSamples[index].param - eSamples.first().param) / span, 1000.0)
            val param1 = 0.0.lerp((eData[index].param - eData.first().param) / span, 1000.0)
            color = Color.getHSBColor(0.1f, 1f, 0.5f)
            stroke = if (index in setOf(0, eFrontRemainIdx, eMergeIdxBegin, eMergeIdxEnd, eBackRemainIdx, rowLast)) BasicStroke(2f)
            else BasicStroke(1f)
            draw(Ellipse2D.Double(param0, param1, pixel * 1.0, pixel * 1.0))
        }
        oSamples.indices.forEach { index ->
            val param0 = 0.0.lerp((oSamples[index].param - oSamples.first().param) / span, 600.0)
            val param1 = 0.0.lerp((oData[index].param - oData.first().param) / span, 600.0)
            color = Color.getHSBColor(0.2f, 1f, 0.5f)
            stroke = if (index in setOf(0, oFrontRemainIdx, oMergeIdxBegin, oMergeIdxEnd, oBackRemainIdx, columnLast)) BasicStroke(2f)
            else BasicStroke(1f)
            draw(Ellipse2D.Double(param0, param1, pixel * 1.0, pixel * 1.0))
        }

        val mergedData = ridge.map { (i, j) -> eSamples[i].lerp(mergeRate, oSamples[j]).weighted() }
        val data = MergeData(overlapState.grade,
                eData.take(eMergeIdxBegin) + oData.take(oMergeIdxBegin),
                eData.drop(eMergeIdxEnd + 1) + oData.drop(oMergeIdxEnd + 1),
                mergedData)
        val update2 = CompareSettings.merger2.generate(data)
        drawCubicBSpline(update2, DrawStyle())
        drawPoints(update2.evaluateAll(0.01), DrawStyle(color = Color.RED))
    }
}

class TransformParam(val s2: Double, val s3: Double, val t0: Double, val t1: Double, val t2: Double, val t3: Double, val t4: Double, val t5: Double) {
    val mergeGrad = (s3 - s2).divOrDefault(t3 - t2) { 1.0 }
    val aFront = 0.5 * (mergeGrad - 1) / (t2 - t1)
    val bFront = 1 - 2 * aFront * t1
    val cFront = s2 - aFront * t2 * t2 - bFront * t2
    val aBack = 0.5 * (mergeGrad - 1) / (t3 - t4)
    val bBack = 1 - 2 * aBack * t4
    val cBack = s3 - aBack * t3 * t3 - bBack * t3
    val s1 = aFront * t1 * t1 + bFront * t1 + cFront
    val s4 = aBack * t4 * t4 + bBack * t4 + cBack
    operator fun invoke(t: Double) = when (t) {
        in t2..t3 -> mergeGrad * t + s2 - mergeGrad * t2
        in t1..t2 -> aFront * t * t + bFront * t + cFront
        in t3..t4 -> aBack * t * t + bBack * t + cBack
        in t0..t1 -> t + s1 - t1
        in t4..t5 -> t + s4 - t4
        else -> error("")
    }
}*/
