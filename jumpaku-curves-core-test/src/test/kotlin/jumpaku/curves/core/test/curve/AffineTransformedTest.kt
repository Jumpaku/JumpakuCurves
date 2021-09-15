package jumpaku.curves.core.test.curve

import jumpaku.curves.core.curve.AffineTransformed
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.curve.bezier.closeTo
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.transform.Translate
import jumpaku.curves.core.transform.UniformlyScale
import org.apache.commons.math3.util.FastMath
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class AffineTransformedTest {

    private val bc = Bezier(
        Point.xyr(-2.0, 0.0, 1.0),
        Point.xyr(-1.0, 0.0, 2.0),
        Point.xy(0.0, 2.0),
        Point.xyr(1.0, 0.0, 2.0),
        Point.xyr(2.0, 0.0, 1.0)
    )

    private val transform = UniformlyScale(2.0)
        .andThen(Rotate(Vector(0.0, 0.0, 1.0), FastMath.PI / 2))
        .andThen(Translate(Vector(1.0, 1.0, 0.0)))

    @Test
    fun testAffineTransformed() {
        print("AffineTransformed")
        val e = bc.affineTransform(transform)
        val a = AffineTransformed(bc, transform)
        Interval.Unit.sample(10).forEach { t ->
            assertThat(a(t), `is`(closeTo(e(t))))
        }
        assertThat(a.domain, `is`(closeTo(e.domain)))
        assertThat(a.originalCurve, `is`(closeTo(bc)))
    }
}