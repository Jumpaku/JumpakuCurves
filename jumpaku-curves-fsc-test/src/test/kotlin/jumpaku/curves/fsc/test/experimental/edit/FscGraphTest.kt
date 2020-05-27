package jumpaku.curves.fsc.test.experimental.edit

import jumpaku.commons.control.None
import jumpaku.commons.control.Option
import jumpaku.commons.control.Some
import jumpaku.commons.control.some
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.KnotVector
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.experimental.edit.Element
import jumpaku.curves.fsc.experimental.edit.FscGraph
import jumpaku.curves.fsc.experimental.edit.Id
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class FscGraphTest {

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

    val g = FscGraph.of(
            elements = (vs0 + vs1 + vs2).withIndex().associate { (i, v) -> Id("$i") to v },
            outgoing = out0 + out1 + out2,
            incoming = in0 + in1 + in2)

    val p0 = FscGraph.of((0..4).zip(vs0).associate { (i, v) -> Id("$i") to v }, out0, in0)
    val p1 = FscGraph.of((5..7).zip(vs1).associate { (i, v) -> Id("$i") to v }, out1, in1)
    val p2 = FscGraph.of((8..11).zip(vs2).associate { (i, v) -> Id("$i") to v }, out2, in2)

    @Test
    fun testProperties() {
        println("Properties")
        assertThat(g.vertices, `is`((0..11).map { Id("$it") }.toSet()))
        assertThat(g.edges, `is`((out0 + out1 + out2).flatMap { (k, v) -> v.map { FscGraph.Edge(k, it) } }.toSet()))
        assertThat(g.edges, `is`((in0 + in1 + in2).flatMap { (k, v) -> v.map { FscGraph.Edge(it, k) } }.toSet()))
    }

    @Test
    fun testCompose() {
        println("Compose")
        assertThat(FscGraph.compose(listOf(p0, p1, p2)), `is`(closeTo(g)))
        assertThat(p0.compose(p1).compose(p2), `is`(closeTo(g)))
        assertThat(p1.compose(p2).compose(p0), `is`(closeTo(g)))
        assertThat(p2.compose(p0).compose(p1), `is`(closeTo(g)))
    }

    @Test
    fun testNextOf() {
        println("NextOf")
        fun assertNextOf(id: Int, exp: Option<Int>) {
            val actual = g.nextOf(Id("$id"))
            val expected = exp.map { Id("$it") }
            assertThat(actual.isDefined, `is`(expected.isDefined))
            if (actual is Some) assertThat(actual.orThrow(), `is`(expected.orThrow()))
        }
        assertNextOf(0, Some(1))
        assertNextOf(1, Some(2))
        assertNextOf(2, Some(3))
        assertNextOf(3, Some(4))
        assertNextOf(4, None)
        assertNextOf(5, Some(6))
        assertNextOf(6, Some(7))
        assertNextOf(7, None)
        assertNextOf(8, Some(9))
        assertNextOf(9, Some(10))
        assertNextOf(10, Some(11))
        assertNextOf(11, Some(8))
    }

    @Test
    fun testPrevOf() {
        println("PrevOf")
        fun assertPrevOf(id: Int, exp: Option<Int>) {
            val actual = g.prevOf(Id("$id"))
            val expected = exp.map { Id("$it") }
            assertThat(actual.isDefined, `is`(expected.isDefined))
            if (actual is Some) assertThat(actual.orThrow(), `is`(expected.orThrow()))
        }
        assertPrevOf(0, None)
        assertPrevOf(1, Some(0))
        assertPrevOf(2, Some(1))
        assertPrevOf(3, Some(2))
        assertPrevOf(4, Some(3))
        assertPrevOf(5, None)
        assertPrevOf(6, Some(5))
        assertPrevOf(7, Some(6))
        assertPrevOf(8, Some(11))
        assertPrevOf(9, Some(8))
        assertPrevOf(10, Some(9))
        assertPrevOf(11, Some(10))
    }

    @Test
    fun testRemove() {
        println("Remove")
        fun edge(s: Int, d: Int): FscGraph.Edge = FscGraph.Edge(Id("$s"), Id("$d"))
        fun Set<Int>.elms(): Set<Id> = map { Id("$it") }.toSet()
        val edges = g.edges

        assertThat(g.remove(setOf()).edges, `is`(edges))
        assertThat(g.remove(setOf(0).elms()).edges, `is`(edges - setOf(edge(0, 1))))
        assertThat(g.remove(setOf(1).elms()).edges, `is`(edges - setOf(edge(0, 1), edge(1, 2))))
        assertThat(g.remove(setOf(2, 3).elms()).edges, `is`(edges - setOf(edge(1, 2), edge(2, 3), edge(3, 4))))
        assertThat(g.remove(setOf(8, 11).elms()).edges, `is`(edges - setOf(edge(8, 9), edge(11, 8), edge(10, 11))))
    }

    @Test
    fun testInsert() {
        println("Insert")
        val a0 = FscGraph.compose(listOf(p1, p2)).insert(mapOf(
                Id("0") to v0,
                Id("1") to v1,
                Id("2") to v2,
                Id("3") to v3,
                Id("4") to v4
        )).insert(insertedEdges = setOf(
                FscGraph.Edge(Id("0"), Id("1")),
                FscGraph.Edge(Id("1"), Id("2")),
                FscGraph.Edge(Id("2"), Id("3")),
                FscGraph.Edge(Id("3"), Id("4"))
        ))
        assertThat(a0, `is`(closeTo(g)))

        val a1 = FscGraph.compose(listOf(p0, p2)).insert(mapOf(
                Id("5") to v5,
                Id("6") to v6,
                Id("7") to v7
        )).insert(insertedEdges = setOf(
                FscGraph.Edge(Id("5"), Id("6")),
                FscGraph.Edge(Id("6"), Id("7"))
        ))
        assertThat(a1, `is`(closeTo(g)))

        val a2 = FscGraph.compose(listOf(p0, p1)).insert(mapOf(
                Id("8") to v8,
                Id("9") to v9,
                Id("10") to v10,
                Id("11") to v11
        )).insert(insertedEdges = setOf(
                FscGraph.Edge(Id("8"), Id("9")),
                FscGraph.Edge(Id("9"), Id("10")),
                FscGraph.Edge(Id("10"), Id("11")),
                FscGraph.Edge(Id("11"), Id("8"))
        ))
        assertThat(a2, `is`(closeTo(g)))
    }

    @Test
    fun testUpdateValue() {
        println("UpdateValue")
        val e0 = Element.Connector(Point.xr(0.0, 4.0), None, Some(Point.xr(1.0, 2.0)))
        val a0 = g.updateValue(Id("0")) { e0 }
        assertThat(a0.edges, `is`(g.edges))
        assertThat(a0[Id("0")], `is`(closeTo(e0)))

        val e1 = Element.Target(BSpline(listOf(
                Point.xr(1.0, 2.0),
                Point.xyr(2.0, 1.0, 2.0),
                Point.xyr(3.0, 1.0, 2.0),
                Point.xr(4.0, 2.0)),
                KnotVector.clamped(Interval(1.0, 3.0), 2, 7)))
        val a1 = g.updateValue(Id("1")) { e1 }
        assertThat(a1.edges, `is`(g.edges))
        assertThat(a1[Id("1")], `is`(closeTo(e1)))

        val e2 = Element.Connector(Point.xr(25.0, 4.0), Some(Point.xr(24.0, 2.0)), Some(Point.xr(24.0, 2.0)))
        val a2 = g.updateValue(Id("9")) { e2 }
        assertThat(a2.edges, `is`(g.edges))
        assertThat(a2[Id("9")], `is`(closeTo(e2)))
    }

    @Test
    fun testConnect() {
        println("Connect")
        val e0 = Element.Target(BSpline(listOf(
                Point.xr(11.0, 2.0),
                Point.xr(12.0, 2.0),
                Point.xr(13.0, 2.0),
                Point.xr(14.0, 2.0)),
                KnotVector.clamped(Interval(1.0, 3.0), 2, 7)))
        val e1 = Element.Connector(Point.xr(15.0, 4.0), Some(Point.xr(14.0, 2.0)), Some(Point.xr(16.0, 2.0)))
        val e2 = Element.Connector(Point.xr(15.0, 4.0), Some(Point.xr(14.0, 2.0)), Some(Point.xr(16.0, 2.0)))
        val e3 = Element.Target(BSpline(listOf(
                Point.xr(16.0, 2.0),
                Point.xr(17.0, 2.0),
                Point.xr(18.0, 2.0),
                Point.xr(19.0, 2.0)),
                KnotVector.clamped(Interval(1.0, 3.0), 2, 7)))
        val e4 = Element.Connector(Point.xr(15.0, 2.0), Some(Point.xr(14.0, 2.0)), Some(Point.xr(16.0, 2.0)))

        val a = FscGraph.of(
                mapOf(Id("0") to e0, Id("1") to e1, Id("2") to e2, Id("3") to e3),
                mapOf(Id("0") to Some(Id("1")), Id("1") to None, Id("2") to Some(Id("3")), Id("3") to None),
                mapOf(Id("0") to None, Id("1") to Some(Id("0")), Id("2") to None, Id("3") to Some(Id("2"))))
                .connect(Id("1"), Id("2")) { _, _ -> Id("4") to e4 }
        assertThat(a, `is`(closeTo(p1)))
    }

    @Test
    fun testDecompose() {
        println("Decompose")
        val a0 = p0.decompose()
        assertThat(a0.size, `is`(1))
        assertThat(a0.first(), `is`(closeTo(p0)))
        val a1 = p1.decompose()
        assertThat(a1.size, `is`(1))
        assertThat(a1.first(), `is`(closeTo(p1)))
        val a2 = p2.decompose()
        assertThat(a2.size, `is`(1))
        assertThat(a2.first(), `is`(closeTo(p2)))

        val a = g.decompose()
        val e = setOf(p0, p1, p2)
        a.forEach { ai -> assertThat(e.any { ei -> isCloseTo(ai, ei) }, `is`(true)) }
        e.forEach { ei -> assertThat(a.any { ai -> isCloseTo(ai, ei) }, `is`(true)) }
    }
/*
    @Test
    fun testToString() {
        println("ToString")
        val actual = g.toString().parseJson().tryMap { FscGraph.fromJson(it) }.orThrow()
        assertThat(actual, `is`(closeTo(g)))
    }*/
}
