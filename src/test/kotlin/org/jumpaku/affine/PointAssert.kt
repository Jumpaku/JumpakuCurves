package org.jumpaku.affine

import org.apache.commons.math3.util.Precision
import org.assertj.core.api.AbstractAssert

/**
 * Created by jumpaku on 2017/05/10.
 */

fun pointAssertThat(actual: Point): PointAssert = PointAssert(actual)

class PointAssert(actual: Point) : AbstractAssert<PointAssert, Point>(actual, PointAssert::class.java) {
    companion object{
        fun assertThat(actual: Point): PointAssert = PointAssert(actual)
    }

    fun isEqualToPoint(expected: Point): PointAssert {
        isNotNull

        if(!Precision.equals(actual.r, expected.r, 1.0e-10)
                || !Precision.equals(actual.x, expected.x, 1.0e-10)
                || !Precision.equals(actual.y, expected.y, 1.0e-10)
                || !Precision.equals(actual.z, expected.z, 1.0e-10)){
            failWithMessage("Expected point to be <%s> but was <%s>", expected.toString(), actual.toString())
            return this
        }

        return this
    }
}
