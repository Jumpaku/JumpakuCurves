package jumpaku.curves.fsc.test.generate

import io.vavr.collection.Array
import jumpaku.commons.json.parseJson
import jumpaku.commons.test.math.closeTo
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.generate.Fuzzifier
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class FuzzifierTest {

    val l = Fuzzifier.Linear(velocityCoefficient = 3.0, accelerationCoefficient = 0.1)

    @Test
    fun testFuzzify() {
        println("Fuzzify")
        val b = BSpline(
                Array.of(Point.xy(-1.0, 0.0), Point.xy(-1.0, 1.0), Point.xy(0.0, 1.0), Point.xy(0.0, 0.0), Point.xy(1.0, 0.0)),
                KnotVector.clamped(Interval(1.0, 2.0), 3, 9))
        val f = l.fuzzify(b, b.domain.sample(10))

        val db = b.derivative
        val ddb = db.derivative
        b.domain.sample(10).forEachIndexed { i, t ->
            val e = 3 * db(t).length() + 0.1 * ddb(t).length()
            val a = f[i]
            assertThat(a, `is`(closeTo(e, 1e0)))
        }
    }

    @Test
    fun testToString() {
        println("ToString")
        val a = l.toString().parseJson().tryMap { Fuzzifier.fromJson(it) }.orThrow()
        assertThat(a, `is`(closeTo(l)))
    }
}