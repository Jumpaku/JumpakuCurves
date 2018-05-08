package jumpaku.fsc.oldtest.generate

import io.vavr.API
import org.apache.commons.math3.linear.ArrayRealVector
import org.assertj.core.api.Assertions
import jumpaku.core.affine.Point
import jumpaku.core.curve.Interval
import jumpaku.core.curve.KnotVector
import jumpaku.core.affine.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fit.createModelMatrix
import jumpaku.core.testold.curve.bspline.bSplineAssertThat
import jumpaku.fsc.generate.nonNegativeLinearLeastSquare
import org.junit.Test


class NnlsTest {

    @Test
    fun testNonNegativeLinearLeastSquareFitting() {
        println("NonNegativeLinearLeastSquareFitting")
        val b0 = BSpline(
                API.Array(Point.x(1.0), Point.x(2.0), Point.x(2.0), Point.x(1.0), Point.x(1.0)),
                KnotVector.clamped(Interval(-1.0, 1.0), 3, 9))
        val data0 = b0.domain.sample(100).map { ParamPoint(b0(it), it) }
        val targetVector0 = data0.map { (p, _) -> p.x } .toJavaArray(Double::class.java).run(::ArrayRealVector)
        val modelMatrix0 = createModelMatrix(data0.map(ParamPoint::param), 3, b0.knotVector)
        val cp0 = API.Array(*nonNegativeLinearLeastSquare(modelMatrix0, targetVector0)
                .toArray().toTypedArray()).map { Point.x(it) }
        bSplineAssertThat(BSpline(cp0, b0.knotVector)).isEqualToBSpline(b0, 1.0e-8)//
    }

    @Test
    fun testNonNegativeLinearLeastSquareNonNegative() {
        println("NonNegativeLinearLeastSquareNonNegative")
        val b1 = BSpline(
                API.Array(Point.x(-1.0), Point.x(2.0), Point.x(-2.0), Point.x(1.0), Point.x(-1.0)),
                KnotVector.clamped(Interval(-1.0, 1.0), 3, 9))
        val data1 = b1.domain.sample(100).map { ParamPoint(b1(it), it) }
        val targetVector1 = data1.map { (p, _) -> p.x } .toJavaArray(Double::class.java).run(::ArrayRealVector)
        val modelMatrix1 = createModelMatrix(data1.map(ParamPoint::param), 3, b1.knotVector)
        val a1 = nonNegativeLinearLeastSquare(modelMatrix1, targetVector1).toArray()
        Assertions.assertThat(a1.size).isEqualTo(5)
        Assertions.assertThat(a1.all { it > 0.0 }).isTrue()
    }

    @Test
    fun testNonNegativeLinearModelValue() {
        println("NonNegativeLinearModelValue")

    }
}