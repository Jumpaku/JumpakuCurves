package jumpaku.core.curve

import com.github.salomonbrys.kotson.fromJson
import jumpaku.core.json.parseToJson
import org.assertj.core.api.Assertions.*
import jumpaku.core.json.prettyGson
import org.junit.Test


class KnotVectorTest {

    @Test
    fun testProperties() {
        println("Properties")
        val k = KnotVector.clampedUniform(3.5, 5.0, 3, 10)
        assertThat(k.knots.size()).isEqualTo(10)
        assertThat(k.knots[0]).isEqualTo(3.5, withPrecision(1.0e-10))
        assertThat(k.knots[1]).isEqualTo(3.5, withPrecision(1.0e-10))
        assertThat(k.knots[2]).isEqualTo(3.5, withPrecision(1.0e-10))
        assertThat(k.knots[3]).isEqualTo(3.5, withPrecision(1.0e-10))
        assertThat(k.knots[4]).isEqualTo(4.0, withPrecision(1.0e-10))
        assertThat(k.knots[5]).isEqualTo(4.5, withPrecision(1.0e-10))
        assertThat(k.knots[6]).isEqualTo(5.0, withPrecision(1.0e-10))
        assertThat(k.knots[7]).isEqualTo(5.0, withPrecision(1.0e-10))
        assertThat(k.knots[8]).isEqualTo(5.0, withPrecision(1.0e-10))
        assertThat(k.knots[9]).isEqualTo(5.0, withPrecision(1.0e-10))

        assertThat(k.size()).isEqualTo(10)
        assertThat(k[0]).isEqualTo(3.5, withPrecision(1.0e-10))
        assertThat(k[1]).isEqualTo(3.5, withPrecision(1.0e-10))
        assertThat(k[2]).isEqualTo(3.5, withPrecision(1.0e-10))
        assertThat(k[3]).isEqualTo(3.5, withPrecision(1.0e-10))
        assertThat(k[4]).isEqualTo(4.0, withPrecision(1.0e-10))
        assertThat(k[5]).isEqualTo(4.5, withPrecision(1.0e-10))
        assertThat(k[6]).isEqualTo(5.0, withPrecision(1.0e-10))
        assertThat(k[7]).isEqualTo(5.0, withPrecision(1.0e-10))
        assertThat(k[8]).isEqualTo(5.0, withPrecision(1.0e-10))
        assertThat(k[9]).isEqualTo(5.0, withPrecision(1.0e-10))

        assertThat(k.degree).isEqualTo(3)
        intervalAssertThat(k.domain).isEqualToInterval(Interval(3.5, 5.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        val k = KnotVector.clampedUniform(3.5, 5.0, 3, 10)
        knotVectorAssertThat(k.toString().parseToJson().get().knotVector).isEqualToKnotVector(k)
    }

    @Test
    fun testLastIndexUnder() {
        println("LastIndexUnder")
        val k = KnotVector.clampedUniform(3.5, 5.0, 3, 10)
        assertThat(k.lastIndexUnder(3.5)).isEqualTo(3)
        assertThat(k.lastIndexUnder(3.7)).isEqualTo(3)
        assertThat(k.lastIndexUnder(4.0)).isEqualTo(4)
        assertThat(k.lastIndexUnder(4.1)).isEqualTo(4)
        assertThat(k.lastIndexUnder(4.5)).isEqualTo(5)
        assertThat(k.lastIndexUnder(4.6)).isEqualTo(5)
        assertThat(k.lastIndexUnder(5.0)).isEqualTo(5)
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val a = KnotVector.clampedUniform(3.5, 5.0, 3, 10).reverse()
        val e = KnotVector.clampedUniform(3.5, 5.0, 3, 10)
        knotVectorAssertThat(a).isEqualToKnotVector(e)
    }

    @Test
    fun testDerivativeKnotVector() {
        println("DerivativeKnotVector")
        val a = KnotVector.clampedUniform(3.5, 5.0, 3, 10).derivativeKnotVector()

        val e = KnotVector.clampedUniform(3.5, 5.0, 2, 8)
        knotVectorAssertThat(a).isEqualToKnotVector(e)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val k = KnotVector.clampedUniform(3.5, 5.0, 3, 10)

        val a0 = k.subdivide(3.5)
        knotVectorAssertThat(a0._1()).isEqualToKnotVector(KnotVector.clampedUniform(3.5, 3.5, 3, 8))
        knotVectorAssertThat(a0._2()).isEqualToKnotVector(KnotVector.clampedUniform(3.5, 5.0, 3, 10))

        val a1 = k.subdivide(3.7)
        knotVectorAssertThat(a1._1()).isEqualToKnotVector(KnotVector.clampedUniform(3.5, 3.7, 3, 8))
        knotVectorAssertThat(a1._2()).isEqualToKnotVector(KnotVector
                .ofKnots(3, Knot(3.7, 4), Knot(4.0), Knot(4.5), Knot(5.0, 4)))

        val a2 = k.subdivide(4.0)
        knotVectorAssertThat(a2._1()).isEqualToKnotVector(KnotVector.clampedUniform(3.5, 4.0, 3, 8))
        knotVectorAssertThat(a2._2()).isEqualToKnotVector(KnotVector.clampedUniform(4.0, 5.0, 3, 9))

        val a3 = k.subdivide(4.2)
        knotVectorAssertThat(a3._1()).isEqualToKnotVector(KnotVector
                .ofKnots(3, Knot(3.5, 4), Knot(4.0), Knot(4.2, 4)))
        knotVectorAssertThat(a3._2()).isEqualToKnotVector(KnotVector
                .ofKnots(3, Knot(4.2, 4), Knot(4.5), Knot(5.0, 4)))

        val a4 = k.subdivide(4.5)
        knotVectorAssertThat(a4._1()).isEqualToKnotVector(KnotVector.clampedUniform(3.5, 4.5, 3, 9))
        knotVectorAssertThat(a4._2()).isEqualToKnotVector(KnotVector.clampedUniform(4.5, 5.0, 3, 8))

        val a5 = k.subdivide(4.6)
        knotVectorAssertThat(a5._1()).isEqualToKnotVector(KnotVector
                .ofKnots(3, Knot(3.5, 4), Knot(4.0), Knot(4.5), Knot(4.6, 4)))
        knotVectorAssertThat(a5._2()).isEqualToKnotVector(KnotVector.clampedUniform(4.6, 5.0, 3, 8))

        val a6 = k.subdivide(5.0)
        knotVectorAssertThat(a6._1()).isEqualToKnotVector(KnotVector.clampedUniform(3.5, 5.0, 3, 10))
        knotVectorAssertThat(a6._2()).isEqualToKnotVector(KnotVector.clampedUniform(5.0, 5.0, 3, 8))
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")

        val k = KnotVector.clampedUniform(3.5, 5.0, 2, 8)

        knotVectorAssertThat(k.insertKnot(3.5, 0)).isEqualToKnotVector(k)
        knotVectorAssertThat(k.insertKnot(3.5, 1)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 3.5, 4.0, 4.5, 5.0, 5.0, 5.0))
        knotVectorAssertThat(k.insertKnot(3.5, 2)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 3.5, 3.5, 4.0, 4.5, 5.0, 5.0, 5.0))
        knotVectorAssertThat(k.insertKnot(3.5, 3)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 3.5, 3.5, 3.5, 4.0, 4.5, 5.0, 5.0, 5.0))
        knotVectorAssertThat(k.insertKnot(3.5, 4)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 3.5, 3.5, 3.5, 3.5, 4.0, 4.5, 5.0, 5.0, 5.0))

        knotVectorAssertThat(k.insertKnot(4.1, 0)).isEqualToKnotVector(k)
        knotVectorAssertThat(k.insertKnot(4.1, 1)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 4.0, 4.1, 4.5, 5.0, 5.0, 5.0))
        knotVectorAssertThat(k.insertKnot(4.1, 2)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 4.0, 4.1, 4.1, 4.5, 5.0, 5.0, 5.0))
        knotVectorAssertThat(k.insertKnot(4.1, 3)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 4.0, 4.1, 4.1, 4.1, 4.5, 5.0, 5.0, 5.0))
        knotVectorAssertThat(k.insertKnot(4.1, 4)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 4.0, 4.1, 4.1, 4.1, 4.1, 4.5, 5.0, 5.0, 5.0))

        knotVectorAssertThat(k.insertKnot(4.5, 0)).isEqualToKnotVector(k)
        knotVectorAssertThat(k.insertKnot(4.5, 1)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 4.0, 4.5, 4.5, 5.0, 5.0, 5.0))
        knotVectorAssertThat(k.insertKnot(4.5, 2)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 4.0, 4.5, 4.5, 4.5, 5.0, 5.0, 5.0))
        knotVectorAssertThat(k.insertKnot(4.5, 3)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 4.0, 4.5, 4.5, 4.5, 4.5, 5.0, 5.0, 5.0))
        knotVectorAssertThat(k.insertKnot(4.5, 4)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 4.0, 4.5, 4.5, 4.5, 4.5, 4.5, 5.0, 5.0, 5.0))

        knotVectorAssertThat(k.insertKnot(5.0, 0)).isEqualToKnotVector(k)
        knotVectorAssertThat(k.insertKnot(5.0, 1)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 4.0, 4.5, 5.0, 5.0, 5.0, 5.0))
        knotVectorAssertThat(k.insertKnot(5.0, 2)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 4.0, 4.5, 5.0, 5.0, 5.0, 5.0, 5.0))
        knotVectorAssertThat(k.insertKnot(5.0, 3)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 4.0, 4.5, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0))
        knotVectorAssertThat(k.insertKnot(5.0, 4)).isEqualToKnotVector(KnotVector(3, 3.5, 3.5, 3.5, 4.0, 4.5, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0))
    }
}