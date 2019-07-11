package jumpaku.curves.experimental.fsc.test.edit

import jumpaku.commons.control.None
import jumpaku.commons.control.Some
import jumpaku.commons.control.none
import jumpaku.commons.json.parseJson
import jumpaku.commons.test.matcher
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.curve.bspline.closeTo
import jumpaku.curves.core.test.curve.bspline.isCloseTo
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.test.geom.isCloseTo
import jumpaku.curves.experimental.fsc.edit.Element
import org.hamcrest.Matchers.`is`
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.assertThat
import org.junit.Test

fun isCloseTo(actual: Element, expected: Element, error: Double = 1.0e-9): Boolean =
        when(actual){
            is Element.Connector -> expected is Element.Connector &&
                    isCloseTo(actual.body, expected.body, error) &&
                    actual.front.isDefined == expected.front.isDefined &&
                    (if (actual.front.isDefined)isCloseTo(actual.front.orThrow(), expected.front.orThrow(), error) else true) &&
                    actual.back.isDefined == expected.back.isDefined &&
                    (if (actual.back.isDefined)isCloseTo(actual.back.orThrow(), expected.back.orThrow(), error) else true)
            is Element.Identified -> expected is Element.Identified &&
                    isCloseTo(actual.fragment, expected.fragment, error)
        }

fun closeTo(expected: Element, precision: Double = 1.0e-9): TypeSafeMatcher<Element> =
        matcher("close to <$expected> with precision $precision") { actual ->
            isCloseTo(actual, expected, precision)
        }

class ElementTest {

    val connector = Element.Connector(
            Point.xyr(1.0, 2.0, 3.0),
            Some(Point.xyr(4.0, 5.0, 6.0)),
            None)

    val target = Element.Identified(BSpline(
            listOf(Point.x(1.0), Point.x(2.0), Point.x(3.0), Point.x(4.0)),
            KnotVector.clamped(Interval(5.0, 6.0), 3, 8)))

    @Test
    fun testConnector() {
        println("Connector")
        assertThat(connector.body, `is`(closeTo(Point.xyr(1.0, 2.0, 3.0))))
        assertThat(connector.front.orThrow(), `is`(closeTo(Point.xyr(4.0, 5.0, 6.0))))
        assertThat(connector.back, `is`(none()))
    }

    @Test
    fun testIdentified() {
        println("Identified")
        assertThat(target.fragment, `is`(closeTo(BSpline(
                listOf(Point.x(1.0), Point.x(2.0), Point.x(3.0), Point.x(4.0)),
                KnotVector.clamped(Interval(5.0, 6.0), 3, 8)))))
        assertThat(target.front, `is`(closeTo(Point.x(1.0))))
        assertThat(target.back, `is`(closeTo(Point.x(4.0))))
    }

    @Test
    fun testToString() {
        println("ToString")
        assertThat(Element.fromJson(connector.toString().parseJson().orThrow()), `is`(closeTo(connector)))
        assertThat(Element.fromJson(target.toString().parseJson().orThrow()), `is`(closeTo(target)))
    }
}