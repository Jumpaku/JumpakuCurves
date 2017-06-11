package org.jumpaku.core.fsci

import io.vavr.collection.Array
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.TimeSeriesPoint
import org.jumpaku.core.affine.Vector
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.fitting.BSplineFitting
import org.jumpaku.core.fitting.createModelMatrix
import org.jumpaku.core.fitting.nonNegativeLinearLeastSquare


fun generateFuzziness(velocity: Vector, acceleration: Vector): Double {
    val velocityCoefficient: Double = 0.004
    val accelerationCoefficient: Double = 0.003
    return velocityCoefficient*velocity.length() + accelerationCoefficient*acceleration.length()
}

class FscGeneration(val degree: Int = 3, val knotSpan: Double = 0.1) {

    fun generate(data: Array<TimeSeriesPoint>): BSpline {
        val sortedData = data.sortBy(TimeSeriesPoint::time)
        val modifiedData = DataModification(knotSpan / 10, knotSpan, knotSpan, degree - 1)
                .modify(data)
        val modifiedDomain = Interval(modifiedData.head().time, modifiedData.last().time)

        val bSpline = BSplineFitting(degree, modifiedDomain, knotSpan).fit(modifiedData)
        val targetVector = createFuzzinessDataVector(modifiedData.map(TimeSeriesPoint::time), bSpline)
        val modelMatrix = createModelMatrix(modifiedData.map(TimeSeriesPoint::time), degree, bSpline.knotValues)
        val fuzzyControlPoints = nonNegativeLinearLeastSquare(modelMatrix, targetVector).toArray()
                .zip(bSpline.controlPoints, { r, (x, y, z) -> Point.xyzr(x, y, z, r) })
        return BSpline(fuzzyControlPoints, bSpline.knots)
                .restrict(Interval(sortedData.head().time, sortedData.last().time))
    }

    fun createFuzzinessDataVector(modifiedDataTimes: Array<Double>, crispBSpline: BSpline): RealVector {
        val derivative1 = crispBSpline.derivative
        val derivative2 = derivative1.derivative
        return modifiedDataTimes
                .map { generateFuzziness(derivative1(it), derivative2(it)) }
                .toJavaArray(Double::class.java)
                .run(::ArrayRealVector)
    }
}