package org.jumpaku.core.fsci

import io.vavr.collection.Array
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.fitting.BSplineFitting
import org.jumpaku.core.fitting.createModelMatrix
import org.jumpaku.core.fitting.nonNegativeLinearLeastSquare


class FscGeneration(val degree: Int = 3, val knotSpan: Double = 0.1) {

    fun generate(data: Array<TimeSeriesPoint>): BSpline {
        val modifiedData = DataPreparing(knotSpan / degree, knotSpan, knotSpan, degree - 1)
                .prepare(data.sortBy(TimeSeriesPoint::time))
        val bSpline = BSplineFitting(
                degree, Interval(modifiedData.head().time, modifiedData.last().time), knotSpan).fit(modifiedData)

        val targetVector = createFuzzinessDataVector(modifiedData.map(TimeSeriesPoint::time), bSpline)
        val modelMatrix = createModelMatrix(modifiedData.map(TimeSeriesPoint::time), degree, bSpline.knotValues)
        val fuzzyControlPoints = nonNegativeLinearLeastSquare(modelMatrix, targetVector)
                .toArray().zip(bSpline.controlPoints, { r, (x, y, z) -> Point.xyzr(x, y, z, r) })

        val fsc = BSpline(fuzzyControlPoints, bSpline.knots)
        return fsc
                .restrict(fsc.knots.tail().head().value, fsc.knots.init().last().value)
    }

    private fun createFuzzinessDataVector(modifiedDataTimes: Array<Double>, crispBSpline: BSpline): RealVector {
        val derivative1 = crispBSpline.derivative
        val derivative2 = derivative1.derivative
        return modifiedDataTimes
                .map { generateFuzziness(derivative1(it), derivative2(it)) }
                .toJavaArray(Double::class.java)
                .run(::ArrayRealVector)
    }
}