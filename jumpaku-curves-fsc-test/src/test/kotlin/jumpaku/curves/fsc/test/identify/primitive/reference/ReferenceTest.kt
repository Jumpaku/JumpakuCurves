package jumpaku.curves.fsc.test.identify.primitive.reference

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.fsc.identify.primitive.reference.Reference
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import kotlin.math.sqrt

class ReferenceTest {

    val r2 = sqrt(2.0)

    val circular = Reference(
            ConicSection(Point.xy(-r2 / 2, -r2 / 2), Point.xy(0.0, 1.0), Point.xy(r2 / 2, -r2 / 2), -r2 / 2),
            Interval(-0.5, 1.5))

    val linear = Reference(
            ConicSection.lineSegment(Point.x(-1.0), Point.x(1.0)),
            Interval(-0.25, 1.25))

    @Test
    fun testEvaluate() {
        println("Evaluate")
        assertThat(circular(-0.5), `is`(closeTo(Point.xy(0.0, -1.0))))
        assertThat(circular(0.0), `is`(closeTo(Point.xy(-r2 / 2, -r2 / 2))))
        assertThat(circular(0.5), `is`(closeTo(Point.xy(0.0, 1.0))))
        assertThat(circular(1.0), `is`(closeTo(Point.xy(r2 / 2, -r2 / 2))))
        assertThat(circular(1.5), `is`(closeTo(Point.xy(0.0, -1.0))))

        assertThat(linear(-0.25), `is`(closeTo(Point.x(-2.0))))
        assertThat(linear(0.0), `is`(closeTo(Point.x(-1.0))))
        assertThat(linear(0.5), `is`(closeTo(Point.x(0.0))))
        assertThat(linear(1.0), `is`(closeTo(Point.x(1.0))))
        assertThat(linear(1.25), `is`(closeTo(Point.x(2.0))))
    }

    @Test
    fun testToString() {
        println("ToString")
        assertThat(circular.toString().parseJson().tryMap { Reference.fromJson(it) }.orThrow(), `is`(closeTo(circular)))
        assertThat(linear.toString().parseJson().tryMap { Reference.fromJson(it) }.orThrow(), `is`(closeTo(linear)))
    }
}