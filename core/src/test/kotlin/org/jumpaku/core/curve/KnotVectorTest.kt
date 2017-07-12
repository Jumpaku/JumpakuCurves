package org.jumpaku.core.curve

import com.github.salomonbrys.kotson.fromJson
import org.assertj.core.api.Assertions.*
import org.jumpaku.core.json.prettyGson
import org.junit.Test


class KnotVectorTest {

    @Test
    fun testClampedUniform() {
        println("ClampedUniform")
        val a0 = KnotVector.clampedUniform(3.5, 5.0, 3, 10)
        knotVectorAssertThat(a0).isEqualToKnotVector(KnotVector(Knot(3.5, 4), Knot(4.0), Knot(4.5), Knot(5.0, 4)))

        val a1 = KnotVector.clampedUniform(3.5, 5.0, 3, 0.5)
        knotVectorAssertThat(a1).isEqualToKnotVector(KnotVector(Knot(3.5, 4), Knot(4.0), Knot(4.5), Knot(5.0, 4)))

        val a2 = KnotVector.clampedUniform(Interval(3.5, 5.0), 3, 10)
        knotVectorAssertThat(a2).isEqualToKnotVector(KnotVector(Knot(3.5, 4), Knot(4.0), Knot(4.5), Knot(5.0, 4)))

        val a3 = KnotVector.clampedUniform(Interval(3.5, 5.0), 3, 0.5)
        knotVectorAssertThat(a3).isEqualToKnotVector(KnotVector(Knot(3.5, 4), Knot(4.0), Knot(4.5), Knot(5.0, 4)))

        val a4 = KnotVector.clampedUniform(3, 10)
        knotVectorAssertThat(a4).isEqualToKnotVector(KnotVector(Knot(0.0, 4), Knot(1.0), Knot(2.0), Knot(3.0, 4)))

        val a5 = KnotVector.clamped(3, 2.0, 4.0, 5.0, 23.4)
        knotVectorAssertThat(a5).isEqualToKnotVector(KnotVector(Knot(2.0, 4), Knot(4.0), Knot(5.0), Knot(23.4, 4)))
    }

    @Test
    fun testProperties() {
        println("Properties")
        val k = KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 2), Knot(3.0, 1), Knot(4.0, 1))
        assertThat(k.value.size()).isEqualTo(8)
        assertThat(k.value[0]).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(k.value[1]).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(k.value[2]).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(k.value[3]).isEqualTo(1.5, withPrecision(1.0e-10))
        assertThat(k.value[4]).isEqualTo(2.0, withPrecision(1.0e-10))
        assertThat(k.value[5]).isEqualTo(2.0, withPrecision(1.0e-10))
        assertThat(k.value[6]).isEqualTo(3.0, withPrecision(1.0e-10))
        assertThat(k.value[7]).isEqualTo(4.0, withPrecision(1.0e-10))

        assertThat(k.size()).isEqualTo(8)
        assertThat(k[0]).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(k[1]).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(k[2]).isEqualTo(1.0, withPrecision(1.0e-10))
        assertThat(k[3]).isEqualTo(1.5, withPrecision(1.0e-10))
        assertThat(k[4]).isEqualTo(2.0, withPrecision(1.0e-10))
        assertThat(k[5]).isEqualTo(2.0, withPrecision(1.0e-10))
        assertThat(k[6]).isEqualTo(3.0, withPrecision(1.0e-10))
        assertThat(k[7]).isEqualTo(4.0, withPrecision(1.0e-10))

        assertThat(k.multiplicity.size()).isEqualTo(8)
        assertThat(k.multiplicity[0]).isEqualTo(3)
        assertThat(k.multiplicity[1]).isEqualTo(3)
        assertThat(k.multiplicity[2]).isEqualTo(3)
        assertThat(k.multiplicity[3]).isEqualTo(1)
        assertThat(k.multiplicity[4]).isEqualTo(2)
        assertThat(k.multiplicity[5]).isEqualTo(2)
        assertThat(k.multiplicity[6]).isEqualTo(1)
        assertThat(k.multiplicity[7]).isEqualTo(1)

        assertThat(k.knots.size()).isEqualTo(5)
        knotAssertThat(k.knots[0]).isEqualToKnot(Knot(1.0, 3))
        knotAssertThat(k.knots[1]).isEqualToKnot(Knot(1.5, 1))
        knotAssertThat(k.knots[2]).isEqualToKnot(Knot(2.0, 2))
        knotAssertThat(k.knots[3]).isEqualToKnot(Knot(3.0, 1))
        knotAssertThat(k.knots[4]).isEqualToKnot(Knot(4.0, 1))
    }

    @Test
    fun testToString() {
        println("ToString")
        val k = KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 2), Knot(3.0, 1), Knot(4.0, 1))
        knotVectorAssertThat(prettyGson.fromJson<KnotVectorJson>(k.toString()).knotVector()).isEqualToKnotVector(k)
    }

    @Test
    fun testLastIndexUnder() {
        println("LastIndexUnder")
        val k = KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 2), Knot(3.0, 1), Knot(4.0, 1))
        assertThat(k.lastIndexUnder(1.0)).isEqualTo(2)
        assertThat(k.lastIndexUnder(1.2)).isEqualTo(2)
        assertThat(k.lastIndexUnder(1.5)).isEqualTo(3)
        assertThat(k.lastIndexUnder(1.7)).isEqualTo(3)
        assertThat(k.lastIndexUnder(2.0)).isEqualTo(5)
        assertThat(k.lastIndexUnder(2.6)).isEqualTo(5)
        assertThat(k.lastIndexUnder(3.0)).isEqualTo(6)
        assertThat(k.lastIndexUnder(4.0)).isEqualTo(7)
    }

    @Test
    fun testReverse() {
        println("Reverse")
        val k = KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 2), Knot(3.0, 1), Knot(4.0, 1))

        val e2 = KnotVector(Knot(-1.0, 1), Knot(0.0, 1), Knot(1.0, 2), Knot(1.5, 1), Knot(2.0, 3))
        knotVectorAssertThat(k.reverse(2)).isEqualToKnotVector(e2)
        val e3 = KnotVector(Knot(-0.5, 1), Knot(0.5, 1), Knot(1.5, 2), Knot(2.0, 1), Knot(2.5, 3))
        knotVectorAssertThat(k.reverse(3)).isEqualToKnotVector(e3)
    }

    @Test
    fun testInnerKnotVector() {
        println("InnerKnotVector")
        val k = KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 2), Knot(3.0, 1), Knot(4.0, 1))

        val e = KnotVector(Knot(1.0, 2), Knot(1.5, 1), Knot(2.0, 2), Knot(3.0, 1))
        knotVectorAssertThat(k.innerKnotVector()).isEqualToKnotVector(e)
    }

    @Test
    fun testSubdivide() {
        println("Subdivide")
        val k = KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 2), Knot(3.0, 1), Knot(4.0, 1))

        val a0 = k.subdivide(2, 1.0)
        knotVectorAssertThat(a0._1()).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 2), Knot(3.0, 1), Knot(4.0, 1)))

        val a1 = k.subdivide(2, 1.2)
        knotVectorAssertThat(a1._1()).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.2, 3)))
        knotVectorAssertThat(a1._2()).isEqualToKnotVector(KnotVector(Knot(1.2, 3), Knot(1.5, 1), Knot(2.0, 2), Knot(3.0, 1), Knot(4.0, 1)))

        val a2 = k.subdivide(2, 1.5)
        knotVectorAssertThat(a2._1()).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 3)))
        knotVectorAssertThat(a2._2()).isEqualToKnotVector(KnotVector(Knot(1.5, 3), Knot(2.0, 2), Knot(3.0, 1), Knot(4.0, 1)))

        val a3 = k.subdivide(2, 1.7)
        knotVectorAssertThat(a3._1()).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(1.7, 3)))
        knotVectorAssertThat(a3._2()).isEqualToKnotVector(KnotVector(Knot(1.7, 3), Knot(2.0, 2), Knot(3.0, 1), Knot(4.0, 1)))

        val a4 = k.subdivide(2, 2.0)
        knotVectorAssertThat(a4._1()).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 3)))
        knotVectorAssertThat(a4._2()).isEqualToKnotVector(KnotVector(Knot(2.0, 3), Knot(3.0, 1), Knot(4.0, 1)))

        val l = KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 3))
        val a5 = l.subdivide(2, 2.0)
        knotVectorAssertThat(a5._1()).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 3)))
    }

    @Test
    fun testInsertKnot() {
        println("InsertKnot")

        val k2 = KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1))

        knotVectorAssertThat(k2.insertKnot(2, 1.0,-1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 1.0, 0)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 1.0, 1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 1.0, 2)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 1.0, 3)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 1.0, 4)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))

        knotVectorAssertThat(k2.insertKnot(2, 1.5,-1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 1.5, 0)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 1.5, 1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 3), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 1.5, 2)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 3), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 1.5, 3)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 3), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 1.5, 4)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 3), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))

        knotVectorAssertThat(k2.insertKnot(2, 1.7,-1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 1.7, 0)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 1.7, 1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(1.7, 1), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 1.7, 2)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(1.7, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 1.7, 3)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(1.7, 3), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 1.7, 4)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(1.7, 3), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))

        knotVectorAssertThat(k2.insertKnot(2, 2.0,-1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 2.0, 0)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 2.0, 1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 2), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 2.0, 2)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 3), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 2.0, 3)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 3), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 2.0, 4)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 3), Knot(3.0, 2), Knot(4.0, 1)))

        knotVectorAssertThat(k2.insertKnot(2, 3.0,-1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 3.0, 0)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.insertKnot(2, 3.0, 1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 3)))
        knotVectorAssertThat(k2.insertKnot(2, 3.0, 2)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 3)))
        knotVectorAssertThat(k2.insertKnot(2, 3.0, 3)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 3)))
        knotVectorAssertThat(k2.insertKnot(2, 3.0, 4)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 3)))


        val k3 = KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1))

        knotVectorAssertThat(k3.insertKnot(3, 1.5,-1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.insertKnot(3, 1.5, 0)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.insertKnot(3, 1.5, 1)).isEqualToKnotVector(KnotVector(Knot(1.0, 2), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.insertKnot(3, 1.5, 2)).isEqualToKnotVector(KnotVector(Knot(1.0, 1), Knot(1.5, 3), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.insertKnot(3, 1.5, 3)).isEqualToKnotVector(KnotVector(Knot(1.5, 4), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.insertKnot(3, 1.5, 4)).isEqualToKnotVector(KnotVector(Knot(1.5, 4), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))

        knotVectorAssertThat(k3.insertKnot(3, 1.7,-1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.insertKnot(3, 1.7, 0)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.insertKnot(3, 1.7, 1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(1.7, 1), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.insertKnot(3, 1.7, 2)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(1.7, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.insertKnot(3, 1.7, 3)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(1.7, 3), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.insertKnot(3, 1.7, 4)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(1.7, 4), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))

        knotVectorAssertThat(k3.insertKnot(3, 2.0,-1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.insertKnot(3, 2.0, 0)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.insertKnot(3, 2.0, 1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 2), Knot(3.0, 2)))
        knotVectorAssertThat(k3.insertKnot(3, 2.0, 2)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 3), Knot(3.0, 1)))
        knotVectorAssertThat(k3.insertKnot(3, 2.0, 3)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 4)))
        knotVectorAssertThat(k3.insertKnot(3, 2.0, 4)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 4)))
    }

    @Test
    fun testMultiplyKnot() {
        println("MultiplyKnot")
        val k2 = KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1))

        knotVectorAssertThat(k2.multiplyKnot(2, 1,-1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 1, 0)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 1, 1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 1, 2)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 1, 3)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 1, 4)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))

        knotVectorAssertThat(k2.multiplyKnot(2, 3,-1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 3, 0)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 3, 1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 3), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 3, 2)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 3), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 3, 3)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 3), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 3, 4)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 3), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))

        knotVectorAssertThat(k2.multiplyKnot(2, 5,-1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 5, 0)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 5, 1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 2), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 5, 2)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 3), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 5, 3)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 3), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 5, 4)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 3), Knot(3.0, 2), Knot(4.0, 1)))

        knotVectorAssertThat(k2.multiplyKnot(2, 7,-1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 7, 0)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k2.multiplyKnot(2, 7, 1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 3)))
        knotVectorAssertThat(k2.multiplyKnot(2, 7, 2)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 3)))
        knotVectorAssertThat(k2.multiplyKnot(2, 7, 3)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 3)))
        knotVectorAssertThat(k2.multiplyKnot(2, 7, 4)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 3)))


        val k3 = KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1))

        knotVectorAssertThat(k3.multiplyKnot(3, 3,-1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.multiplyKnot(3, 3, 0)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.multiplyKnot(3, 3, 1)).isEqualToKnotVector(KnotVector(Knot(1.0, 2), Knot(1.5, 2), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.multiplyKnot(3, 3, 2)).isEqualToKnotVector(KnotVector(Knot(1.0, 1), Knot(1.5, 3), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.multiplyKnot(3, 3, 3)).isEqualToKnotVector(KnotVector(Knot(1.5, 4), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.multiplyKnot(3, 3, 4)).isEqualToKnotVector(KnotVector(Knot(1.5, 4), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))

        knotVectorAssertThat(k3.multiplyKnot(3, 4,-1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.multiplyKnot(3, 4, 0)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 1), Knot(3.0, 2), Knot(4.0, 1)))
        knotVectorAssertThat(k3.multiplyKnot(3, 4, 1)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 2), Knot(3.0, 2)))
        knotVectorAssertThat(k3.multiplyKnot(3, 4, 2)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 3), Knot(3.0, 1)))
        knotVectorAssertThat(k3.multiplyKnot(3, 4, 3)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 4)))
        knotVectorAssertThat(k3.multiplyKnot(3, 4, 4)).isEqualToKnotVector(KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 4)))
    }

    @Test
    fun testIndexCloseTo() {
        println("IndexCloseTo")
        val k = KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 2), Knot(3.0, 1), Knot(4.0, 1))

        assertThat(k.indexCloseTo(1.0).get()).isIn(0, 1, 2)
        assertThat(k.indexCloseTo(1.2).isEmpty).isTrue()
        assertThat(k.indexCloseTo(1.5).get()).isEqualTo(3)
        assertThat(k.indexCloseTo(1.7).isEmpty).isTrue()
        assertThat(k.indexCloseTo(2.0).get()).isIn(4, 5)
        assertThat(k.indexCloseTo(2.6).isEmpty).isTrue()
        assertThat(k.indexCloseTo(3.0).get()).isEqualTo(6)
        assertThat(k.indexCloseTo(4.0).get()).isEqualTo(7)
    }

    @Test
    fun testClampInsertionTimes() {
        println("ClampInsertionTimes")
        val k = KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 2), Knot(3.0, 1), Knot(4.0, 1))
        assertThat(k.clampInsertionTimes(2, 1.0,-1)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(2, 1.0, 0)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(2, 1.0, 1)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(2, 1.0, 2)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(2, 1.0, 3)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(2, 1.0, 4)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(3, 1.0,-1)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(3, 1.0, 0)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(3, 1.0, 1)).isEqualTo(1)
        assertThat(k.clampInsertionTimes(3, 1.0, 2)).isEqualTo(1)
        assertThat(k.clampInsertionTimes(3, 1.0, 3)).isEqualTo(1)
        assertThat(k.clampInsertionTimes(3, 1.0, 4)).isEqualTo(1)

        assertThat(k.clampInsertionTimes(2, 1.2,-1)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(2, 1.2, 0)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(2, 1.2, 1)).isEqualTo(1)
        assertThat(k.clampInsertionTimes(2, 1.2, 2)).isEqualTo(2)
        assertThat(k.clampInsertionTimes(2, 1.2, 3)).isEqualTo(3)
        assertThat(k.clampInsertionTimes(2, 1.2, 4)).isEqualTo(3)
        assertThat(k.clampInsertionTimes(3, 1.2,-1)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(3, 1.2, 0)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(3, 1.2, 1)).isEqualTo(1)
        assertThat(k.clampInsertionTimes(3, 1.2, 2)).isEqualTo(2)
        assertThat(k.clampInsertionTimes(3, 1.2, 3)).isEqualTo(3)
        assertThat(k.clampInsertionTimes(3, 1.2, 4)).isEqualTo(4)

        assertThat(k.clampInsertionTimes(2, 1.5,-1)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(2, 1.5, 0)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(2, 1.5, 1)).isEqualTo(1)
        assertThat(k.clampInsertionTimes(2, 1.5, 2)).isEqualTo(2)
        assertThat(k.clampInsertionTimes(2, 1.5, 3)).isEqualTo(2)
        assertThat(k.clampInsertionTimes(2, 1.5, 4)).isEqualTo(2)
        assertThat(k.clampInsertionTimes(3, 1.5,-1)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(3, 1.5, 0)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(3, 1.5, 1)).isEqualTo(1)
        assertThat(k.clampInsertionTimes(3, 1.5, 2)).isEqualTo(2)
        assertThat(k.clampInsertionTimes(3, 1.5, 3)).isEqualTo(3)
        assertThat(k.clampInsertionTimes(3, 1.5, 4)).isEqualTo(3)

        assertThat(k.clampInsertionTimes(2, 2.0,-1)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(2, 2.0, 0)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(2, 2.0, 1)).isEqualTo(1)
        assertThat(k.clampInsertionTimes(2, 2.0, 2)).isEqualTo(1)
        assertThat(k.clampInsertionTimes(2, 2.0, 3)).isEqualTo(1)
        assertThat(k.clampInsertionTimes(2, 2.0, 4)).isEqualTo(1)
        assertThat(k.clampInsertionTimes(3, 2.0,-1)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(3, 2.0, 0)).isEqualTo(0)
        assertThat(k.clampInsertionTimes(3, 2.0, 1)).isEqualTo(1)
        assertThat(k.clampInsertionTimes(3, 2.0, 2)).isEqualTo(2)
        assertThat(k.clampInsertionTimes(3, 2.0, 3)).isEqualTo(2)
        assertThat(k.clampInsertionTimes(3, 2.0, 4)).isEqualTo(2)
    }

    @Test
    fun testDomain() {
        println("Domain")
        val k = KnotVector(Knot(1.0, 3), Knot(1.5, 1), Knot(2.0, 2), Knot(3.0, 1), Knot(4.0, 1))

        val domain2 = k.domain(2)
        val domain3 = k.domain(3)
        intervalAssertThat(domain2).isEqualToInterval(Interval(1.0, 2.0))
        intervalAssertThat(domain3).isEqualToInterval(Interval(1.5, 2.0))
    }

}