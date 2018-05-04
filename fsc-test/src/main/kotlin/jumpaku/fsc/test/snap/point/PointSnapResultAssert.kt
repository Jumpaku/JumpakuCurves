package jumpaku.fsc.test.snap.point

import jumpaku.core.testold.affine.pointAssertThat
import jumpaku.fsc.snap.point.PointSnapResult
import jumpaku.fsc.test.snap.gridAssertThat
import jumpaku.fsc.test.snap.gridPointAssertThat
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

fun pointSnapResultAssertThat(actual: PointSnapResult): PointSnapResultAssert = PointSnapResultAssert(actual)
class PointSnapResultAssert(actual: PointSnapResult) : AbstractAssert<PointSnapResultAssert, PointSnapResult>(actual, PointSnapResultAssert::class.java) {

    fun isEqualToPointSnapResult(expected: PointSnapResult, eps: Double = 1.0e-10): PointSnapResultAssert {
        isNotNull

        Assertions.assertThat(actual.grade.value).isEqualTo(expected.grade.value, Assertions.withPrecision(eps))
        pointAssertThat(actual.worldPoint).isEqualToPoint(expected.worldPoint)
        gridPointAssertThat(actual.gridPoint).isEqualToGridPoint(expected.gridPoint)
        gridAssertThat(actual.grid).isEqualToGrid(expected.grid)

        return this
    }
}