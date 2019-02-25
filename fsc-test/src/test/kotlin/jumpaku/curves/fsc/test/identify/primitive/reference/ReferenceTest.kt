package jumpaku.curves.fsc.test.identify.primitive.reference

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.json.parseJson
import jumpaku.curves.core.test.geom.shouldEqualToPoint
import jumpaku.curves.fsc.identify.primitive.reference.Reference
import org.junit.jupiter.api.Test
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
        circular(-0.5).shouldEqualToPoint(Point.xy(0.0, -1.0))
        circular(0.0).shouldEqualToPoint(Point.xy(-r2/2, -r2/2))
        circular(0.5).shouldEqualToPoint(Point.xy(0.0, 1.0))
        circular(1.0).shouldEqualToPoint(Point.xy(r2/2, -r2/2))
        circular(1.5).shouldEqualToPoint(Point.xy(0.0, -1.0))

        linear(-0.25).shouldEqualToPoint(Point.x(-2.0))
        linear(0.0).shouldEqualToPoint(Point.x(-1.0))
        linear(0.5).shouldEqualToPoint(Point.x(0.0))
        linear(1.0).shouldEqualToPoint(Point.x(1.0))
        linear(1.25).shouldEqualToPoint(Point.x(2.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        circular.toString().parseJson().tryMap { Reference.fromJson(it) }.orThrow().shouldEqualToReference(circular)
        linear.toString().parseJson().tryMap { Reference.fromJson(it) }.orThrow().shouldEqualToReference(linear)
    }
}