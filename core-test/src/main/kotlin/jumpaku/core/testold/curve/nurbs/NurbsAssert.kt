package jumpaku.core.testold.curve.nurbs

import jumpaku.core.curve.nurbs.Nurbs
import jumpaku.core.testold.affine.weightedPointAssertThat
import jumpaku.core.testold.curve.knotVectorAssertThat
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

fun nurbsAssertThat(actual: Nurbs): NurbsAssert = NurbsAssert(actual)
class NurbsAssert(actual: Nurbs) : AbstractAssert<NurbsAssert, Nurbs>(actual, NurbsAssert::class.java) {

    fun isEqualToNurbs(expected: Nurbs, eps: Double = 1.0e-10): NurbsAssert {
        isNotNull

        Assertions.assertThat(actual.controlPoints.size()).`as`("controlPoints size").isEqualTo(expected.controlPoints.size())

        actual.weightedControlPoints.zip(expected.weightedControlPoints)
                .forEachIndexed {
                    i, (a, e) -> weightedPointAssertThat(a).`as`("nurbs.weightedControlPoints[%d]", i).isEqualToWeightedPoint(e, eps)
                }

        Assertions.assertThat(actual.knotVector.knots.size()).`as`("knotVector size").isEqualTo(expected.knotVector.knots.size())

        knotVectorAssertThat(actual.knotVector).isEqualToKnotVector(expected.knotVector)

        return this
    }
}