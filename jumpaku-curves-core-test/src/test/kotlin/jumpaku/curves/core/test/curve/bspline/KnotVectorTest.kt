package jumpaku.curves.core.test.curve.bspline

import jumpaku.curves.core.test.closeTo
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bspline.KnotVector
import jumpaku.curves.core.test.curve.closeTo
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test


class KnotVectorTest {

    @Test
    fun testClamped() {
        println("Clamped")
        val a0 = KnotVector.clamped(Interval(1.0, 4.0), 3, 10)
        assertThat(a0, `is`(closeTo(listOf(1.0, 1.0, 1.0, 1.0, 2.0, 3.0, 4.0, 4.0, 4.0, 4.0))))
        assertThat(a0.degree, `is`(3))
        assertThat(a0.domain, `is`(closeTo(Interval(1.0, 4.0))))

        val a1 = KnotVector.clamped(Interval(1.0, 4.0), 3, 1.0)
        assertThat(a0, `is`(closeTo(listOf(1.0, 1.0, 1.0, 1.0, 2.0, 3.0, 4.0, 4.0, 4.0, 4.0))))
        assertThat(a1.degree, `is`(3))
        assertThat(a1.domain, `is`(closeTo(Interval(1.0, 4.0))))
    }

    @Test
    fun testProperties() {
        println("Properties")
        val k = KnotVector.clamped(Interval(3.5, 5.0), 3, 10)
        assertThat(k, `is`(closeTo(listOf(3.5, 3.5, 3.5, 3.5, 4.0, 4.5, 5.0, 5.0, 5.0, 5.0))))
        assertThat(k.degree, `is`(3))
        assertThat(k.domain, `is`(closeTo(Interval(3.5, 5.0))))
    }

    @Test
    fun testSearchIndexToInsert() {
        println("SearchIndexToInsert")
        val k = KnotVector.clamped(Interval(3.5, 5.0), 3, 10)
        assertThat(k.searchIndexToInsert(3.5), `is`(3))
        assertThat(k.searchIndexToInsert(3.7), `is`(3))
        assertThat(k.searchIndexToInsert(4.0), `is`(4))
        assertThat(k.searchIndexToInsert(4.1), `is`(4))
        assertThat(k.searchIndexToInsert(4.5), `is`(5))
        assertThat(k.searchIndexToInsert(4.6), `is`(5))
        assertThat(k.searchIndexToInsert(5.0), `is`(6))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val a = KnotVector.clamped(Interval(3.5, 5.0), 3, 10).reverse()
        val e = KnotVector.clamped(Interval(3.5, 5.0), 3, 10)
        assertThat(a, `is`(closeTo(e)))
    }

    @Test
    fun testDifferentiate() {
        println("Differentiate")
        val a = KnotVector.clamped(Interval(3.5, 5.0), 3, 10).differentiate()
        val e = KnotVector.clamped(Interval(3.5, 5.0), 2, 8)
        assertThat(a, `is`(closeTo(e)))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val k = KnotVector.clamped(Interval(3.5, 5.0), 3, 10)
        val (a00, a01) = k.subdivide(3.5)
        assertThat(a00, `is`(closeTo(listOf(3.5, 3.5, 3.5, 3.5, 3.5, 3.5, 3.5, 3.5))))
        assertThat(a01, `is`(closeTo(listOf(3.5, 3.5, 3.5, 3.5, 4.0, 4.5, 5.0, 5.0, 5.0, 5.0))))

        val (a10, a11) = k.subdivide(3.7)
        assertThat(a10, `is`(closeTo(listOf(3.5, 3.5, 3.5, 3.5, 3.7, 3.7, 3.7, 3.7))))
        assertThat(a11, `is`(closeTo(listOf(3.7, 3.7, 3.7, 3.7, 4.0, 4.5, 5.0, 5.0, 5.0, 5.0))))

        val (a20, a21) = k.subdivide(4.0)
        assertThat(a20, `is`(closeTo(listOf(3.5, 3.5, 3.5, 3.5, 4.0, 4.0, 4.0, 4.0))))
        assertThat(a21, `is`(closeTo(listOf(4.0, 4.0, 4.0, 4.0, 4.5, 5.0, 5.0, 5.0, 5.0))))

        val (a30, a31) = k.subdivide(4.2)
        assertThat(a30, `is`(closeTo(listOf(3.5, 3.5, 3.5, 3.5, 4.0, 4.2, 4.2, 4.2, 4.2))))
        assertThat(a31, `is`(closeTo(listOf(4.2, 4.2, 4.2, 4.2, 4.5, 5.0, 5.0, 5.0, 5.0))))

        val (a40, a41) = k.subdivide(4.5)
        assertThat(a40, `is`(closeTo(listOf(3.5, 3.5, 3.5, 3.5, 4.0, 4.5, 4.5, 4.5, 4.5))))
        assertThat(a41, `is`(closeTo(listOf(4.5, 4.5, 4.5, 4.5, 5.0, 5.0, 5.0, 5.0))))

        val (a50, a51) = k.subdivide(4.6)
        assertThat(a50, `is`(closeTo(listOf(3.5, 3.5, 3.5, 3.5, 4.0, 4.5, 4.6, 4.6, 4.6, 4.6))))
        assertThat(a51, `is`(closeTo(listOf(4.6, 4.6, 4.6, 4.6, 5.0, 5.0, 5.0, 5.0))))

        val (a60, a61) = k.subdivide(5.0)
        assertThat(a60, `is`(closeTo(listOf(3.5, 3.5, 3.5, 3.5, 4.0, 4.5, 5.0, 5.0, 5.0, 5.0))))
        assertThat(a61, `is`(closeTo(listOf(5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0))))
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")

        val k = KnotVector.clamped(Interval(3.0, 5.0), 2, 7)

        assertThat(k.insertKnot(3.0, 0), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(3.0, 1), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(3.0, 2), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(3.0, 3), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(3.0, 4), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 5.0, 5.0, 5.0))))

        assertThat(k.insertKnot(3.5, 0), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(3.5, 1), `is`(closeTo(listOf(3.0, 3.0, 3.0, 3.5, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(3.5, 2), `is`(closeTo(listOf(3.0, 3.0, 3.0, 3.5, 3.5, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(3.5, 3), `is`(closeTo(listOf(3.0, 3.0, 3.0, 3.5, 3.5, 3.5, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(3.5, 4), `is`(closeTo(listOf(3.0, 3.0, 3.0, 3.5, 3.5, 3.5, 3.5, 4.0, 5.0, 5.0, 5.0))))

        assertThat(k.insertKnot(4.0, 0), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(4.0, 1), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(4.0, 2), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 4.0, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(4.0, 3), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 4.0, 4.0, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(4.0, 4), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 4.0, 4.0, 4.0, 4.0, 5.0, 5.0, 5.0))))

        assertThat(k.insertKnot(4.5, 0), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(4.5, 1), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 4.5, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(4.5, 2), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 4.5, 4.5, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(4.5, 3), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 4.5, 4.5, 4.5, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(4.5, 4), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 4.5, 4.5, 4.5, 4.5, 5.0, 5.0, 5.0))))

        assertThat(k.insertKnot(5.0, 0), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(5.0, 1), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(5.0, 2), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(5.0, 3), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 5.0, 5.0, 5.0))))
        assertThat(k.insertKnot(5.0, 4), `is`(closeTo(listOf(3.0, 3.0, 3.0, 4.0, 5.0, 5.0, 5.0))))

    }
}

