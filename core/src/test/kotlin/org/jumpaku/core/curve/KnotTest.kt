package org.jumpaku.core.curve

import com.github.salomonbrys.kotson.fromJson
import org.assertj.core.api.Assertions.*
import org.jumpaku.core.json.prettyGson
import org.junit.Test


class KnotTest {

    @Test
    fun testProperties() {
        println("Properties")
        val k = Knot(2.0, 4)
        assertThat(k.value).isEqualTo(2.0, withPrecision(1.0e-10))
        assertThat(k.multiplicity).isEqualTo(4)
    }

    @Test
    fun testToArray() {
        println("ToArray")
        val a = Knot(2.0, 4).toArray()
        assertThat(a[0]).isEqualTo(2.0, withPrecision(1.0e-10))
        assertThat(a[1]).isEqualTo(2.0, withPrecision(1.0e-10))
        assertThat(a[2]).isEqualTo(2.0, withPrecision(1.0e-10))
        assertThat(a[3]).isEqualTo(2.0, withPrecision(1.0e-10))
        assertThat(a.size()).isEqualTo(4)
    }

    @Test
    fun testReduceMultiplicity() {
        println("ReduceMultiplicity")
        val k2 = Knot(2.0, 4).reduceMultiplicity(2)
        knotAssertThat(k2).isEqualToKnot(Knot(2.0, 2))

        val k3 = Knot(2.0, 4).reduceMultiplicity()
        knotAssertThat(k3).isEqualToKnot(Knot(2.0, 3))
    }

    @Test
    fun testElevateMultiplicity() {
        println("ElevateMultiplicity")
        val k6 = Knot(2.0, 4).elevateMultiplicity(2)
        knotAssertThat(k6).isEqualToKnot(Knot(2.0, 6))

        val k5 = Knot(2.0, 4).elevateMultiplicity()
        knotAssertThat(k5).isEqualToKnot(Knot(2.0, 5))
    }

    @Test
    fun testToString() {
        println("ToString")
        val k = Knot(2.0, 4)
        knotAssertThat(prettyGson.fromJson<KnotJson>(k.toString()).knot()).isEqualToKnot(k)
    }
}