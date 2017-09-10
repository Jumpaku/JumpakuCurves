package jumpaku.fsc.generate

import io.vavr.collection.Array
import jumpaku.core.affine.Point
import jumpaku.core.curve.Interval
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fit.BSplineFitter
import jumpaku.core.fit.createModelMatrix
import org.apache.commons.math3.linear.ArrayRealVector


class FscGenerator(
        val degree: Int = 3,
        val knotSpan: Double = 0.1,
        val generateFuzziness: (BSpline, Array<Double>)->Array<Double> = { crisp, ts ->
            val derivative1 = crisp.derivative
            val derivative2 = derivative1.derivative
            val velocityCoefficient = 0.004
            val accelerationCoefficient = 0.003
            ts.map {
                val v = derivative1(it).length()
                val a = derivative2(it).length()
                velocityCoefficient * v + a * accelerationCoefficient + 1.0
            }
        }) {

    fun generate(data: Array<ParamPoint>): BSpline {
        require(data.size() >= 2) { "data size(${data.size()}) < 2" }

        val modifiedData = DataPreparer(knotSpan / degree, knotSpan, knotSpan, degree - 1)
                .prepare(data)
        val bSpline = BSplineFitter(
                degree, Interval(modifiedData.head().param, modifiedData.last().param), knotSpan).fit(modifiedData)

        val targetVector = generateFuzziness(bSpline, modifiedData.map(ParamPoint::param))
                .toJavaArray(Double::class.java)
                .run(::ArrayRealVector)
        val modelMatrix = createModelMatrix(modifiedData.map(ParamPoint::param), degree, bSpline.knotVector)
        val fuzzyControlPoints = nonNegativeLinearLeastSquare(modelMatrix, targetVector).toArray()
                .zip(bSpline.controlPoints, { r, (x, y, z) -> Point.xyzr(x, y, z, r) })

        val fsc = BSpline(fuzzyControlPoints, bSpline.knotVector)
        val domain = fsc.knotVector.knots.slice(degree + 1, fsc.knotVector.size() - degree - 1)
                .let { Interval(it.head(), it.last()) }

        return fsc.restrict(domain)
    }
}