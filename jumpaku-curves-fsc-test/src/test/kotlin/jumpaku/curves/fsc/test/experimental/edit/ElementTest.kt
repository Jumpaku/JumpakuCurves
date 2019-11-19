package jumpaku.curves.fsc.test.experimental.edit

import jumpaku.commons.control.None
import jumpaku.commons.control.Some
import jumpaku.commons.control.none
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.curve.bspline.closeTo
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.fsc.experimental.edit.Element
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class ElementTest {

    val connector = Element.Connector(
            Point.xyr(1.0, 2.0, 3.0),
            Some(Point.xyr(4.0, 5.0, 6.0)),
            None)

    val target = Element.Target(BSpline(
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
        println("Target")
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