package jumpaku.curves.fsc.test.experimental.edit

import jumpaku.commons.control.None
import jumpaku.commons.control.Some
import jumpaku.commons.control.result
import jumpaku.commons.control.some
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.experimental.edit.Element
import jumpaku.curves.fsc.experimental.edit.FscGraph
import jumpaku.curves.fsc.experimental.edit.Id
import org.hamcrest.core.Is
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Test

class FscPathTest {


    val v0 = Element.Connector(Point.xr(0.0, 2.0), None, Some(Point.xr(1.0, 2.0)))
    val v1 = Element.Target(BSpline(listOf(
            Point.xr(1.0, 2.0),
            Point.xr(2.0, 2.0),
            Point.xr(3.0, 2.0),
            Point.xr(4.0, 2.0)),
            KnotVector.clamped(Interval(1.0, 3.0), 2, 7)))
    val v2 = Element.Connector(Point.xr(5.0, 2.0), Some(Point.xr(4.0, 2.0)), Some(Point.xr(6.0, 2.0)))
    val v3 = Element.Target(BSpline(listOf(
            Point.xr(6.0, 2.0),
            Point.xr(7.0, 2.0),
            Point.xr(8.0, 2.0),
            Point.xr(9.0, 2.0)),
            KnotVector.clamped(Interval(1.0, 3.0), 2, 7)))
    val v4 = Element.Connector(Point.xr(10.0, 2.0), Some(Point.xr(9.0, 2.0)), None)

    val v5 = Element.Target(BSpline(listOf(
            Point.xr(11.0, 2.0),
            Point.xr(12.0, 2.0),
            Point.xr(13.0, 2.0),
            Point.xr(14.0, 2.0)),
            KnotVector.clamped(Interval(1.0, 3.0), 2, 7)))
    val v6 = Element.Connector(Point.xr(15.0, 2.0), Some(Point.xr(14.0, 2.0)), Some(Point.xr(16.0, 2.0)))
    val v7 = Element.Target(BSpline(listOf(
            Point.xr(16.0, 2.0),
            Point.xr(17.0, 2.0),
            Point.xr(18.0, 2.0),
            Point.xr(19.0, 2.0)),
            KnotVector.clamped(Interval(1.0, 3.0), 2, 7)))

    val v8 = Element.Target(BSpline(listOf(
            Point.xr(21.0, 2.0),
            Point.xr(22.0, 2.0),
            Point.xr(23.0, 2.0),
            Point.xr(24.0, 2.0)),
            KnotVector.clamped(Interval(1.0, 3.0), 2, 7)))
    val v9 = Element.Connector(Point.xr(25.0, 2.0), Some(Point.xr(24.0, 2.0)), Some(Point.xr(24.0, 2.0)))
    val v10 = Element.Target(BSpline(listOf(
            Point.xr(24.0, 2.0),
            Point.xr(23.0, 2.0),
            Point.xr(22.0, 2.0),
            Point.xr(21.0, 2.0)),
            KnotVector.clamped(Interval(1.0, 3.0), 2, 7)))
    val v11 = Element.Connector(Point.xr(20.0, 2.0), Some(Point.xr(21.0, 2.0)), Some(Point.xr(21.0, 2.0)))

    val vs0 = listOf(v0, v1, v2, v3, v4)
    val out0 = mapOf(0 to 1, 1 to 2, 2 to 3, 3 to 4)
            .mapValues { (_, v) -> some(Id("$v")) }.mapKeys { (k, _) -> Id("$k") }
    val in0 = mapOf(0 to 1, 1 to 2, 2 to 3, 3 to 4)
            .map { (k, v) -> v to k }.toMap()
            .mapValues { (_, v) -> some(Id("$v")) }.mapKeys { (k, _) -> Id("$k") }

    val vs1 = listOf(v5, v6, v7)
    val out1 = mapOf(5 to 6, 6 to 7)
            .mapValues { (_, v) -> some(Id("$v")) }.mapKeys { (k, _) -> Id("$k") }
    val in1 = mapOf(5 to 6, 6 to 7).map { (k, v) -> v to k }.toMap()
            .mapValues { (_, v) -> some(Id("$v")) }.mapKeys { (k, _) -> Id("$k") }

    val vs2 = listOf(v8, v9, v10, v11)
    val out2 = mapOf(8 to 9, 9 to 10, 10 to 11, 11 to 8)
            .mapValues { (_, v) -> some(Id("$v")) }.mapKeys { (k, _) -> Id("$k") }
    val in2 = mapOf(8 to 9, 9 to 10, 10 to 11, 11 to 8)
            .map { (k, v) -> v to k }.toMap()
            .mapValues { (_, v) -> some(Id("$v")) }.mapKeys { (k, _) -> Id("$k") }

    val p0 = FscGraph.of((0..4).zip(vs0).associate { (i, v) -> Id("$i") to v }, out0, in0).decompose()[0]
    val p1 = FscGraph.of((5..7).zip(vs1).associate { (i, v) -> Id("$i") to v }, out1, in1).decompose()[0]
    val p2 = FscGraph.of((8..11).zip(vs2).associate { (i, v) -> Id("$i") to v }, out2, in2).decompose()[0]


    @Test
    fun testConnectors() {
        println("Connectors")
        val a0 = p0.connectors()
        val e0 = listOf(v0, v2, v4)
        assertThat(a0.size, `is`(e0.size))
        a0.zip(e0).forEach { (a, e) -> assertThat(a, `is`(closeTo(e))) }

        val a1 = p1.connectors()
        val e1 = listOf(v6)
        assertThat(a1.size, `is`(e1.size))
        a1.zip(e1).forEach { (a, e) -> assertThat(a, `is`(closeTo(e))) }

        val a2 = p2.connectors()
        val e2 = listOf(v9, v11)
        assertThat(a2.size, `is`(e2.size))
        assertThat(a2.all { a -> e2.any { e -> isCloseTo(a, e) } }, `is`(true))
    }

    @Test
    fun testFragments() {
        println("Fragments")
        val a0 = p0.fragments()
        val e0 = listOf(v1, v3)
        assertThat(a0.size, `is`(e0.size))
        a0.zip(e0).forEach { (a, e) -> assertThat(a, `is`(closeTo(e))) }

        val a1 = p1.fragments()
        val e1 = listOf(v5, v7)
        assertThat(a1.size, `is`(e1.size))
        a1.zip(e1).forEach { (a, e) -> assertThat(a, `is`(closeTo(e))) }

        val a2 = p2.fragments()
        val e2 = listOf(v8, v10)
        assertThat(a2.size, `is`(e2.size))
        assertThat(a2.all { a -> e2.any { e -> isCloseTo(a, e) } }, `is`(true))
    }
}
