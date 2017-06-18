package org.jumpaku.core.fsci

import io.vavr.collection.Array
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import org.jumpaku.core.affine.Point
import org.jumpaku.core.fitting.ParamPoint
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.fitting.BSplineFitting
import org.jumpaku.core.fitting.createModelMatrix
import org.jumpaku.core.fitting.nonNegativeLinearLeastSquare


class FscGeneration(val degree: Int = 3, val knotSpan: Double = 0.1) {

    fun generate(data: Array<ParamPoint>): BSpline {
        val modifiedData = DataPreparing(knotSpan / degree, knotSpan, knotSpan, degree - 1)
                .prepare(data.sortBy(ParamPoint::param))
        val bSpline = BSplineFitting(
                degree, Interval(modifiedData.head().param, modifiedData.last().param), knotSpan).fit(modifiedData)

        val targetVector = createFuzzinessDataVector(modifiedData.map(ParamPoint::param), bSpline)
        val modelMatrix = createModelMatrix(modifiedData.map(ParamPoint::param), degree, bSpline.knotVector)
        val fuzzyControlPoints = nonNegativeLinearLeastSquare(modelMatrix, targetVector)
                .toArray().zip(bSpline.controlPoints, { r, (x, y, z) -> Point.xyzr(x, y, z, r) })

        val fsc = BSpline(fuzzyControlPoints, bSpline.knotVector)
        return fsc.restrict(fsc.knotVector.innerKnotVector().domain(degree))
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