package org.jumpaku.curve.bezier

import org.assertj.core.api.AbstractAssert
import org.jumpaku.affine.pointAssertThat
import org.jumpaku.util.*

/**
 * Created by jumpaku on 2017/05/16.
 */

fun bezierAssertThat(actual: Bezier): BezierAssert = BezierAssert(actual)

class BezierAssert(actual: Bezier) : AbstractAssert<BezierAssert, Bezier>(actual, BezierAssert::class.java) {

    companion object{
        fun assertThat(actual: Bezier): BezierAssert = BezierAssert(actual)
    }

    fun isEqualToBezier(expected: Bezier): BezierAssert {
        isNotNull

        if (actual.controlPoints.size() != expected.controlPoints.size()){
            failWithMessage("Expected bezier size to be <%s> but was <%s>", expected.controlPoints.size(), actual.controlPoints.size())
            return this
        }

        actual.controlPoints.zip(expected.controlPoints)
                .forEachIndexed {
                    i, (a, e) -> pointAssertThat(a).`as`("bezier.controlPoints[%d]", i).isEqualToPoint(e)
                }

        return this
    }
}