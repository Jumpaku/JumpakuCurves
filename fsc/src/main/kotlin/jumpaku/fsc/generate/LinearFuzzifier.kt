package jumpaku.fsc.generate

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.geom.Point
import jumpaku.fsc.generate.fit.createModelMatrix
import org.apache.commons.math3.linear.ArrayRealVector

class LinearFuzzifier(
        val velocityCoefficient: Double,
        val accelerationCoefficient: Double): Fuzzifier {

    override fun fuzzify(crisp: BSpline): BSpline {
        val derivative1 = crisp.derivative
        val derivative2 = derivative1.derivative
        val degree = crisp.degree
        val knot = crisp.knotVector
        val n = knot.extractedKnots.size * degree
        val ts = crisp.domain.sample(n)
        val fs = ts.map {
            val v = derivative1(it).length()
            val a = derivative2(it).length()
            velocityCoefficient * v + accelerationCoefficient * a + 1.0
        }
        val targetVector = fs.toDoubleArray().run(::ArrayRealVector)
        val modelMatrix = createModelMatrix(ts, degree, knot)
        val fuzzyControlPoints = nonNegativeLinearLeastSquare(modelMatrix, targetVector).toArray()
                .zip(crisp.controlPoints) { r, (x, y, z) -> Point.xyzr(x, y, z, r) }

        return BSpline(fuzzyControlPoints, knot)
    }
}