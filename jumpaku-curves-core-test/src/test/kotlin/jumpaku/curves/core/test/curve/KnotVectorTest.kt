package jumpaku.curves.core.test.curve

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.Knot
import jumpaku.curves.core.curve.KnotVector
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class KnotVectorTest {

    @Test
    fun testClamped() {
        println("Clamped")
        val a0 = KnotVector.clamped(Interval(1.0, 4.0), 3, 10)
        assertThat(a0.knots.size, `is`(4))
        assertThat(a0.knots[0].value, `is`(1.0))
        assertThat(a0.knots[1].value, `is`(2.0))
        assertThat(a0.knots[2].value, `is`(3.0))
        assertThat(a0.knots[3].value, `is`(4.0))
        assertThat(a0.knots[0].multiplicity, `is`(4))
        assertThat(a0.knots[1].multiplicity, `is`(1))
        assertThat(a0.knots[2].multiplicity, `is`(1))
        assertThat(a0.knots[3].multiplicity, `is`(4))
        assertThat(a0.degree, `is`(3))
        assertThat(a0.domain, `is`(closeTo(Interval(1.0, 4.0))))

        val a1 = KnotVector.clamped(Interval(1.0, 4.0), 3, 1.0)
        assertThat(a1.knots.size, `is`(4))
        assertThat(a1.knots[0].value, `is`(1.0))
        assertThat(a1.knots[1].value, `is`(2.0))
        assertThat(a1.knots[2].value, `is`(3.0))
        assertThat(a1.knots[3].value, `is`(4.0))
        assertThat(a1.knots[0].multiplicity, `is`(4))
        assertThat(a1.knots[1].multiplicity, `is`(1))
        assertThat(a1.knots[2].multiplicity, `is`(1))
        assertThat(a1.knots[3].multiplicity, `is`(4))
        assertThat(a1.degree, `is`(3))
        assertThat(a1.domain, `is`(closeTo(Interval(1.0, 4.0))))
    }

    @Test
    fun testUniform() {
        println("Uniform")
        val a0 = KnotVector.uniform(Interval(1.0, 4.0), 3, 10)
        assertThat(a0.knots.size, `is`(10))
        assertThat(a0.knots[0].value, `is`(-2.0))
        assertThat(a0.knots[1].value, `is`(-1.0))
        assertThat(a0.knots[2].value, `is`(0.0))
        assertThat(a0.knots[3].value, `is`(1.0))
        assertThat(a0.knots[4].value, `is`(2.0))
        assertThat(a0.knots[5].value, `is`(3.0))
        assertThat(a0.knots[6].value, `is`(4.0))
        assertThat(a0.knots[7].value, `is`(5.0))
        assertThat(a0.knots[8].value, `is`(6.0))
        assertThat(a0.knots[9].value, `is`(7.0))
        assertThat(a0.knots[0].multiplicity, `is`(1))
        assertThat(a0.knots[1].multiplicity, `is`(1))
        assertThat(a0.knots[2].multiplicity, `is`(1))
        assertThat(a0.knots[3].multiplicity, `is`(1))
        assertThat(a0.knots[4].multiplicity, `is`(1))
        assertThat(a0.knots[5].multiplicity, `is`(1))
        assertThat(a0.knots[6].multiplicity, `is`(1))
        assertThat(a0.knots[7].multiplicity, `is`(1))
        assertThat(a0.knots[8].multiplicity, `is`(1))
        assertThat(a0.knots[9].multiplicity, `is`(1))
        assertThat(a0.degree, `is`(3))
        assertThat(a0.domain, `is`(closeTo(Interval(1.0, 4.0))))

        val a1 = KnotVector.uniform(Interval(1.0, 4.0), 3, 1.0)
        assertThat(a1.knots.size, `is`(10))
        assertThat(a1.knots[0].value, `is`(-2.0))
        assertThat(a1.knots[1].value, `is`(-1.0))
        assertThat(a1.knots[2].value, `is`(0.0))
        assertThat(a1.knots[3].value, `is`(1.0))
        assertThat(a1.knots[4].value, `is`(2.0))
        assertThat(a1.knots[5].value, `is`(3.0))
        assertThat(a1.knots[6].value, `is`(4.0))
        assertThat(a1.knots[7].value, `is`(5.0))
        assertThat(a1.knots[8].value, `is`(6.0))
        assertThat(a1.knots[9].value, `is`(7.0))
        assertThat(a1.knots[0].multiplicity, `is`(1))
        assertThat(a1.knots[1].multiplicity, `is`(1))
        assertThat(a1.knots[2].multiplicity, `is`(1))
        assertThat(a1.knots[3].multiplicity, `is`(1))
        assertThat(a1.knots[4].multiplicity, `is`(1))
        assertThat(a1.knots[5].multiplicity, `is`(1))
        assertThat(a1.knots[6].multiplicity, `is`(1))
        assertThat(a1.knots[7].multiplicity, `is`(1))
        assertThat(a1.knots[8].multiplicity, `is`(1))
        assertThat(a1.knots[9].multiplicity, `is`(1))
        assertThat(a1.degree, `is`(3))
        assertThat(a1.domain, `is`(closeTo(Interval(1.0, 4.0))))
    }

    val k = KnotVector.clamped(Interval(3.5, 5.0), 3, 10)

    @Test
    fun testProperties() {
        println("Properties")
        assertThat(k.knots.size, `is`(4))
        assertThat(k.knots[0].value, `is`(3.5))
        assertThat(k.knots[1].value, `is`(4.0))
        assertThat(k.knots[2].value, `is`(4.5))
        assertThat(k.knots[3].value, `is`(5.0))
        assertThat(k.knots[0].multiplicity, `is`(4))
        assertThat(k.knots[1].multiplicity, `is`(1))
        assertThat(k.knots[2].multiplicity, `is`(1))
        assertThat(k.knots[3].multiplicity, `is`(4))

        assertThat(k.degree, `is`(3))
        assertThat(k.domain, `is`(closeTo(Interval(3.5, 5.0))))
    }

    @Test
    fun testToString() {
        println("ToString")
        val l = k.toString().parseJson().tryMap { KnotVector.fromJson(it) }.orThrow()
        assertThat(l, `is`(closeTo(k)))
    }

    @Test
    fun testLastIndexUnder() {
        println("LastIndexUnder")
        assertThat(k.searchLastExtractedLessThanOrEqualTo(3.5), `is`(3))
        assertThat(k.searchLastExtractedLessThanOrEqualTo(3.7), `is`(3))
        assertThat(k.searchLastExtractedLessThanOrEqualTo(4.0), `is`(4))
        assertThat(k.searchLastExtractedLessThanOrEqualTo(4.1), `is`(4))
        assertThat(k.searchLastExtractedLessThanOrEqualTo(4.5), `is`(5))
        assertThat(k.searchLastExtractedLessThanOrEqualTo(4.6), `is`(5))
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val e = KnotVector.clamped(Interval(3.5, 5.0), 3, 10)
        assertThat(k.reverse(), `is`(closeTo(e)))
    }

    @Test
    fun testDerivativeKnotVector() {
        println("DerivativeKnotVector")
        val e = KnotVector.clamped(Interval(3.5, 5.0), 2, 8)
        assertThat(k.derivativeKnotVector(), `is`(closeTo(e)))
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (a00, a01) = k.subdivide(3.5)
        assertThat(a00.isDefined, `is`(false))
        assertThat(a01.orThrow(), `is`(closeTo(KnotVector.clamped(Interval(3.5, 5.0), 3, 10))))

        val (a10, a11) = k.subdivide(3.7)
        assertThat(a10.orThrow(), `is`(closeTo(KnotVector.clamped(Interval(3.5, 3.7), 3, 8))))
        assertThat(a11.orThrow(), `is`(closeTo(KnotVector(3,
                Knot(3.7, 4), Knot(4.0), Knot(4.5), Knot(5.0, 4)))))

        val (a20, a21) = k.subdivide(4.0)
        assertThat(a20.orThrow(), `is`(closeTo(KnotVector.clamped(Interval(3.5, 4.0), 3, 8))))
        assertThat(a21.orThrow(), `is`(closeTo(KnotVector.clamped(Interval(4.0, 5.0), 3, 9))))

        val (a30, a31) = k.subdivide(4.2)
        assertThat(a30.orThrow(), `is`(closeTo(KnotVector(3,
                Knot(3.5, 4), Knot(4.0), Knot(4.2, 4)))))
        assertThat(a31.orThrow(), `is`(closeTo(KnotVector(3,
                Knot(4.2, 4), Knot(4.5), Knot(5.0, 4)))))

        val (a40, a41) = k.subdivide(4.5)
        assertThat(a40.orThrow(), `is`(closeTo(KnotVector.clamped(Interval(3.5, 4.5), 3, 9))))
        assertThat(a41.orThrow(), `is`(closeTo(KnotVector.clamped(Interval(4.5, 5.0), 3, 8))))

        val (a50, a51) = k.subdivide(4.6)
        assertThat(a50.orThrow(),
                `is`(closeTo(KnotVector(3, Knot(3.5, 4), Knot(4.0), Knot(4.5), Knot(4.6, 4)))))
        assertThat(a51.orThrow(), `is`(closeTo(KnotVector.clamped(Interval(4.6, 5.0), 3, 8))))

        val (a60, a61) = k.subdivide(5.0)
        assertThat(a60.orThrow(), `is`(closeTo(KnotVector.clamped(Interval(3.5, 5.0), 3, 10))))
        assertThat(a61.isDefined, `is`(false))
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")

        val k = KnotVector(3,
                Knot(2.5), Knot(3.0), Knot(3.5), Knot(4.0), Knot(4.5), Knot(5.0, 2), Knot(5.5))

        assertThat(k.insert(3.5, 0), `is`(closeTo(k)))
        assertThat(k.insert(3.5, 1), `is`(closeTo(KnotVector(3, Knot(2.5), Knot(3.0), Knot(3.5, 2), Knot(4.0), Knot(4.5), Knot(5.0, 2), Knot(5.5)))))
        assertThat(k.insert(3.5, 2), `is`(closeTo(KnotVector(3, Knot(2.5), Knot(3.0), Knot(3.5, 3), Knot(4.0), Knot(4.5), Knot(5.0, 2), Knot(5.5)))))

        assertThat(k.insert(4.1, 0), `is`(closeTo(k)))
        assertThat(k.insert(4.1, 1), `is`(closeTo(KnotVector(3, Knot(2.5), Knot(3.0), Knot(3.5), Knot(4.0), Knot(4.1), Knot(4.5), Knot(5.0, 2), Knot(5.5)))))
        assertThat(k.insert(4.1, 2), `is`(closeTo(KnotVector(3, Knot(2.5), Knot(3.0), Knot(3.5), Knot(4.0), Knot(4.1, 2), Knot(4.5), Knot(5.0, 2), Knot(5.5)))))
        assertThat(k.insert(4.1, 3), `is`(closeTo(KnotVector(3, Knot(2.5), Knot(3.0), Knot(3.5), Knot(4.0), Knot(4.1, 3), Knot(4.5), Knot(5.0, 2), Knot(5.5)))))

        assertThat(k.insert(4.5, 0), `is`(closeTo(k)))
        assertThat(k.insert(4.5, 1), `is`(closeTo(KnotVector(3, Knot(2.5), Knot(3.0), Knot(3.5), Knot(4.0), Knot(4.5, 2), Knot(5.0, 2), Knot(5.5)))))
        assertThat(k.insert(4.5, 2), `is`(closeTo(KnotVector(3, Knot(2.5), Knot(3.0), Knot(3.5), Knot(4.0), Knot(4.5, 3), Knot(5.0, 2), Knot(5.5)))))

        assertThat(k.insert(5.0, 0), `is`(closeTo(k)))
        assertThat(k.insert(5.0, 1), `is`(closeTo(KnotVector(3, Knot(2.5), Knot(3.0), Knot(3.5), Knot(4.0), Knot(4.5), Knot(5.0, 3), Knot(5.5)))))
    }
}