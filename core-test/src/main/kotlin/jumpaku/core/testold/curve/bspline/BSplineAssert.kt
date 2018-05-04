package jumpaku.core.testold.curve.bspline

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.testold.affine.pointAssertThat
import jumpaku.core.testold.curve.knotVectorAssertThat
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

fun bSplineAssertThat(actual: BSpline): BSplineAssert = BSplineAssert(actual)
class BSplineAssert(actual: BSpline) : AbstractAssert<BSplineAssert, BSpline>(actual, BSplineAssert::class.java) {

    fun isEqualToBSpline(expected: BSpline, eps: Double = 1.0e-10): BSplineAssert {
        isNotNull

        Assertions.assertThat(actual.controlPoints.size()).`as`("controlPoints size").isEqualTo(expected.controlPoints.size())

        actual.controlPoints.zip(expected.controlPoints)
                .forEachIndexed {
                    i, (a, e) -> pointAssertThat(a).`as`("bSpline.controlPoints[%d]", i).isEqualToPoint(e, eps)
                }

        Assertions.assertThat(actual.knotVector.knots.size()).`as`("knotVector size").isEqualTo(expected.knotVector.knots.size())

        knotVectorAssertThat(actual.knotVector).isEqualToKnotVector(expected.knotVector)

        return this
    }
}