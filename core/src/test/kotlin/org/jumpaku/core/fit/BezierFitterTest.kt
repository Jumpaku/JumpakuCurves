package org.jumpaku.core.fit

import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.ParamPoint
import org.jumpaku.core.curve.bezier.Bezier
import org.jumpaku.core.curve.bezier.bezierAssertThat
import org.junit.Test


class BezierFitterTest {

    @Test
    fun testFit() {
        println("Fit")
        val b = Bezier(Point.xy(-2.0, 0.0), Point.xy(-1.0, 0.0), Point.xy(0.0, 2.0), Point.xy(1.0, 0.0), Point.xy(2.0, 0.0))
        val data = b.domain.sample(10).map { ParamPoint(b(it), it) }
        val f = BezierFitter(b.degree).fit(data)
        bezierAssertThat(f).isEqualToBezier(b)
    }

}