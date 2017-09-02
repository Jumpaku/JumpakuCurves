package jumpaku.fsc.generate

import io.vavr.collection.Array
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import jumpaku.core.affine.Point
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.Interval
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fit.BSplineFitter
import jumpaku.core.fit.createModelMatrix


class FscGenerator(val degree: Int = 3, val knotSpan: Double = 0.1) {

    fun generate(data: Array<ParamPoint>): BSpline {
        val modifiedData = jumpaku.fsc.generate.DataPreparer(knotSpan / degree, knotSpan, knotSpan, degree - 1)
                .prepare(data.sortBy(ParamPoint::param))
        val bSpline = BSplineFitter(
                degree, Interval(modifiedData.head().param, modifiedData.last().param), knotSpan).fit(modifiedData)

        val targetVector = createFuzzinessDataVector(modifiedData.map(ParamPoint::param), bSpline)
        val modelMatrix = createModelMatrix(modifiedData.map(ParamPoint::param), degree, bSpline.knotVector)
        val fuzzyControlPoints = jumpaku.fsc.generate.nonNegativeLinearLeastSquare(modelMatrix, targetVector).toArray()
                .zip(bSpline.controlPoints, { r, (x, y, z) -> Point.xyzr(x, y, z, r) })

        val fsc = BSpline(fuzzyControlPoints, bSpline.knotVector)
        val domain = fsc.knotVector.knots.slice(degree + 1, fsc.knotVector.size() - degree - 1)
                .let { Interval(it.head(), it.last()) }

        return fsc.restrict(domain)
    }

    private fun createFuzzinessDataVector(modifiedDataTimes: Array<Double>, crispBSpline: BSpline): RealVector {
        val derivative1 = crispBSpline.derivative
        val derivative2 = derivative1.derivative
        return modifiedDataTimes
                .map { jumpaku.fsc.generate.generateFuzziness(derivative1(it), derivative2(it)) }
                .toJavaArray(Double::class.java)
                .run(::ArrayRealVector)
    }
}