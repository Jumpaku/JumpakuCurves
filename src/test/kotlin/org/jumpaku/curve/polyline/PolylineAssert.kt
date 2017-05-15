package org.jumpaku.curve.polyline

import io.vavr.API
import io.vavr.Tuple2
import org.assertj.core.api.AbstractAssert
import org.jumpaku.affine.Point
import org.jumpaku.affine.pointAssertThat
import org.jumpaku.util.*

/**
 * Created by jumpaku on 2017/05/15.
 */

fun polylineAssertThat(actual: Polyline): PolylineAssert = PolylineAssert(actual)

class PolylineAssert(actual: Polyline) : AbstractAssert<PolylineAssert, Polyline>(actual, PolylineAssert::class.java) {

    companion object{
        fun assertThat(actual: Polyline): PolylineAssert = PolylineAssert(actual)
    }

    fun isEqualToPolyline(expected: Polyline): PolylineAssert {
        isNotNull

        if (actual.points.size() != expected.points.size()){
            failWithMessage("Expected polyline size to be <%s> but was <%s>", expected.points.size(), actual.points.size())
            return this
        }

        actual.points.zip(expected.points)
                .forEachIndexed {
                    i, (a, e) -> pointAssertThat(a).`as`("polyline.points[%d]", i).isEqualToPoint(e)
                }

        return this
    }
}
