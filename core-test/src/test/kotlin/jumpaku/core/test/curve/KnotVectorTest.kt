package jumpaku.core.test.curve

import jumpaku.core.curve.Interval
import jumpaku.core.curve.Knot
import jumpaku.core.curve.KnotVector
import jumpaku.core.json.parseJson
import jumpaku.core.test.shouldBeCloseTo
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeFalse
import org.junit.Test

class KnotVectorTest {

    val k = KnotVector.clamped(Interval(3.5, 5.0), 3, 10)

    @Test
    fun testProperties() {
        println("Properties")
        k.knots.size().shouldBe(4)
        k.knots[0].value.shouldBeCloseTo(3.5)
        k.knots[1].value.shouldBeCloseTo(4.0)
        k.knots[2].value.shouldBeCloseTo(4.5)
        k.knots[3].value.shouldBeCloseTo(5.0)
        k.knots[0].multiplicity.shouldBe(4)
        k.knots[1].multiplicity.shouldBe(1)
        k.knots[2].multiplicity.shouldBe(1)
        k.knots[3].multiplicity.shouldBe(4)

        k.degree.shouldBe(3)
        k.domain.shouldBeInterval(Interval(3.5, 5.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        k.toString().parseJson().flatMap { KnotVector.fromJson(it) }.get().shouldBeKnotVector(k)
    }

    @Test
    fun testLastIndexUnder() {
        println("LastIndexUnder")
        k.lastExtractedIndexUnder(3.5).shouldBe(3)
        k.lastExtractedIndexUnder(3.7).shouldBe(3)
        k.lastExtractedIndexUnder(4.0).shouldBe(4)
        k.lastExtractedIndexUnder(4.1).shouldBe(4)
        k.lastExtractedIndexUnder(4.5).shouldBe(5)
        k.lastExtractedIndexUnder(4.6).shouldBe(5)
        k.lastExtractedIndexUnder(5.0).shouldBe(5)
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val e = KnotVector.clamped(Interval(3.5, 5.0), 3, 10)
        k.reverse().shouldBeKnotVector(e)
    }

    @Test
    fun testDerivativeKnotVector() {
        println("DerivativeKnotVector")
        val e = KnotVector.clamped(Interval(3.5, 5.0), 2, 8)
        k.derivativeKnotVector().shouldBeKnotVector(e)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val (a00, a01) = k.subdivide(3.5)
        a00.isDefined.shouldBeFalse()
        a01.get().shouldBeKnotVector(KnotVector.clamped(Interval(3.5, 5.0), 3, 10))

        val (a10, a11) = k.subdivide(3.7)
        a10.get().shouldBeKnotVector(KnotVector.clamped(Interval(3.5, 3.7), 3, 8))
        a11.get().shouldBeKnotVector(KnotVector(3,
                Knot(3.7, 4), Knot(4.0), Knot(4.5), Knot(5.0, 4)))

        val (a20, a21) = k.subdivide(4.0)
        a20.get().shouldBeKnotVector(KnotVector.clamped(Interval(3.5, 4.0), 3, 8))
        a21.get().shouldBeKnotVector(KnotVector.clamped(Interval(4.0, 5.0), 3, 9))

        val (a30, a31) = k.subdivide(4.2)
        a30.get().shouldBeKnotVector(KnotVector(3,
                Knot(3.5, 4), Knot(4.0), Knot(4.2, 4)))
        a31.get().shouldBeKnotVector(KnotVector(3,
                Knot(4.2, 4), Knot(4.5), Knot(5.0, 4)))

        val (a40, a41) = k.subdivide(4.5)
        a40.get().shouldBeKnotVector(KnotVector.clamped(Interval(3.5, 4.5), 3, 9))
        a41.get().shouldBeKnotVector(KnotVector.clamped(Interval(4.5, 5.0), 3, 8))

        val (a50, a51) = k.subdivide(4.6)
        a50.get().shouldBeKnotVector(KnotVector(3,
                Knot(3.5, 4), Knot(4.0), Knot(4.5), Knot(4.6, 4)))
        a51.get().shouldBeKnotVector(KnotVector.clamped(Interval(4.6, 5.0), 3, 8))

        val (a60, a61) = k.subdivide(5.0)
        a60.get().shouldBeKnotVector(KnotVector.clamped(Interval(3.5, 5.0), 3, 10))
        a61.isDefined.shouldBeFalse()
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")

        val k = KnotVector(3,
                Knot(2.5), Knot(3.0), Knot(3.5), Knot(4.0), Knot(4.5), Knot(5.0, 2), Knot(5.5))

        k.insert(3.5, 0).shouldBeKnotVector(k)
        k.insert(3.5, 1).shouldBeKnotVector(KnotVector(3,
                Knot(2.5), Knot(3.0), Knot(3.5, 2), Knot(4.0), Knot(4.5), Knot(5.0, 2), Knot(5.5)))
        k.insert(3.5, 2).shouldBeKnotVector(KnotVector(3,
                Knot(2.5), Knot(3.0), Knot(3.5, 3), Knot(4.0), Knot(4.5), Knot(5.0, 2), Knot(5.5)))

        k.insert(4.1, 0).shouldBeKnotVector(k)
        k.insert(4.1, 1).shouldBeKnotVector(KnotVector(3,
                Knot(2.5), Knot(3.0), Knot(3.5), Knot(4.0), Knot(4.1), Knot(4.5), Knot(5.0, 2), Knot(5.5)))
        k.insert(4.1, 2).shouldBeKnotVector(KnotVector(3,
                Knot(2.5), Knot(3.0), Knot(3.5), Knot(4.0), Knot(4.1, 2), Knot(4.5), Knot(5.0, 2), Knot(5.5)))
        k.insert(4.1, 3).shouldBeKnotVector(KnotVector(3,
                Knot(2.5), Knot(3.0), Knot(3.5), Knot(4.0), Knot(4.1, 3), Knot(4.5), Knot(5.0, 2), Knot(5.5)))

        k.insert(4.5, 0).shouldBeKnotVector(k)
        k.insert(4.5, 1).shouldBeKnotVector(KnotVector(3,
                Knot(2.5), Knot(3.0), Knot(3.5), Knot(4.0), Knot(4.5, 2), Knot(5.0, 2), Knot(5.5)))
        k.insert(4.5, 2).shouldBeKnotVector(KnotVector(3,
                Knot(2.5), Knot(3.0), Knot(3.5), Knot(4.0), Knot(4.5, 3), Knot(5.0, 2), Knot(5.5)))

        k.insert(5.0, 0).shouldBeKnotVector(k)
        k.insert(5.0, 1).shouldBeKnotVector(KnotVector(3,
                Knot(2.5), Knot(3.0), Knot(3.5), Knot(4.0), Knot(4.5), Knot(5.0, 3), Knot(5.5)))
    }
}