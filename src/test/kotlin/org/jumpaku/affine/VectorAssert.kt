package org.jumpaku.affine

import org.apache.commons.math3.util.Precision
import org.assertj.core.api.AbstractAssert

/**
 * Created by jumpaku on 2017/05/10.
 */
fun vectorAssertThat(actual: Vector): VectorAssert = VectorAssert(actual)

class VectorAssert(actual: Vector) : AbstractAssert<VectorAssert, Vector>(actual, VectorAssert::class.java) {

    fun isEqualToVector(expected: Vector): VectorAssert {
        isNotNull

        if(!Precision.equals(actual.x, expected.x, 1.0e-10)
                || !Precision.equals(actual.y, expected.y, 1.0e-10)
                || !Precision.equals(actual.z, expected.z, 1.0e-10)){
            failWithMessage("Expected vector to be <%s> but was <%s>", expected.toString(), actual.toString())
            return this
        }

        return this
    }
}