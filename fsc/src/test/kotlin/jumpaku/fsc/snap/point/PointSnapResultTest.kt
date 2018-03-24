package jumpaku.fsc.snap.point

import jumpaku.core.affine.pointAssertThat
import jumpaku.fsc.snap.GridPoint
import jumpaku.fsc.snap.gridAssertThat
import jumpaku.fsc.snap.gridPointAssertThat
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.Test

fun pointSnapResultAssertThat(actual: PointSnapResult): PointSnapResultAssert = PointSnapResultAssert(actual)

class PointSnapResultAssert(actual: PointSnapResult) : AbstractAssert<PointSnapResultAssert, PointSnapResult>(actual, PointSnapResultAssert::class.java) {

    fun isEqualToPointSnapResult(expected: PointSnapResult, eps: Double = 1.0e-10): PointSnapResultAssert {
        isNotNull

        Assertions.assertThat(actual.grade.value).isEqualTo(expected.grade.value, withPrecision(eps))
        pointAssertThat(actual.worldPoint).isEqualToPoint(expected.worldPoint)
        gridPointAssertThat(actual.gridPoint).isEqualToGridPoint(expected.gridPoint)
        gridAssertThat(actual.grid).isEqualToGrid(expected.grid)

        return this
    }
}

class PointSnapResultTest {

    @Test
    fun testToString() {
        println("ToString")
        fail("ToString not implemented.")
    }

}