package org.jumpaku.core.curve.bezier

import org.assertj.core.api.AbstractAssert
import jumpaku.core.affine.pointAssertThat
import jumpaku.core.curve.bezier.Bezier
import org.jumpaku.core.util.*

/**
 * Created by jumpaku on 2017/05/16.
 */

fun bezierAssertThat(actual: Bezier): BezierAssert = BezierAssert(actual)

class BezierAssert(actual: Bezier) : AbstractAssert<BezierAssert, Bezier>(actual, BezierAssert::class.java) {

    fun isEqualToBezier(expected: Bezier, eps: Double = 1.0e-10): BezierAssert {
        isNotNull

        if (actual.controlPoints.size() != expected.controlPoints.size()){
            failWithMessage("Expected bezier size to be <%s> but was <%s>", expected.controlPoints.size(), actual.controlPoints.size())
            return this
        }

        actual.controlPoints.zip(expected.controlPoints)
                .forEachIndexed {
                    i, (a, e) -> pointAssertThat(a).`as`("bezier.controlPoints[%d]", i).isEqualToPoint(e, eps)
                }

        return this
    }
}