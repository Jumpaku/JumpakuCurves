package jumpaku.curves.core.test.curve

import jumpaku.curves.core.curve.SimilarlyTransformed
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bezier.Bezier
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.curve.bezier.closeTo
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.*
import org.apache.commons.math3.util.FastMath
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class SimilarityTransformedTest {

    private val bc = Bezier(
        Point.xyr(-2.0, 0.0, 1.0),
        Point.xyr(-1.0, 0.0, 2.0),
        Point.xy(0.0, 2.0),
        Point.xyr(1.0, 0.0, 2.0),
        Point.xyr(2.0, 0.0, 1.0)
    )

    private val transform = SimilarityTransform.Identity
        .andThen(UniformlyScale(2.0).asSimilarity())
        .andThen(Rotate(Vector(0.0, 0.0, 1.0), FastMath.PI / 2).asSimilarity())
        .andThen(Translate(Vector(1.0, 1.0, 0.0)).asSimilarity())

    @Test
    fun testSimilarityTransformed() {
        print("SimilarityTransformed")
        val e = bc.similarlyTransform(transform)
        val a = SimilarlyTransformed(bc, transform)
        Interval.Unit.sample(10).forEach { t ->
            assertThat(a(t), `is`(closeTo(e(t))))
        }
        assertThat(a.domain, `is`(closeTo(e.domain)))
        assertThat(a.originalCurve, `is`(closeTo(bc)))
    }
}