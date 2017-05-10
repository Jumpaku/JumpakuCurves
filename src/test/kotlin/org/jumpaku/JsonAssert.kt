package org.jumpaku

import org.apache.commons.math3.util.Precision
import org.assertj.core.api.AbstractAssert
import org.jumpaku.affine.Vector
import org.jumpaku.affine.VectorAssert

/**
 * Created by jumpaku on 2017/05/10.
 */

fun jsonAssertThat(actual: String): JsonAssert = JsonAssert(actual)

class JsonAssert(actual: String) : AbstractAssert<JsonAssert, String>(actual, JsonAssert::class.java) {
    fun isEqualToWithoutWhitespace(expected: Any?): JsonAssert {
        isNotNull

        if (expected !is String) {
            failWithMessage("Expected type to be <%s> but was <%s>", expected?.javaClass, actual.javaClass)
            return this
        }
        if(actual.filterNot { it.isWhitespace() } != expected.filterNot { it.isWhitespace() }){
            failWithMessage("Expected json string to be <%s> but was <%s>", expected.toString(), actual.toString())
            return this
        }

        return this
    }
}