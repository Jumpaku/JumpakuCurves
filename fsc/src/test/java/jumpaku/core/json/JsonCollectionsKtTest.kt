package jumpaku.core.json

import com.github.salomonbrys.kotson.fromJson
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.HashMap
import io.vavr.control.Option
import jumpaku.core.affine.Point
import jumpaku.core.affine.PointJson
import jumpaku.core.affine.pointAssertThat
import org.assertj.core.api.Assertions.*
import org.junit.Test

class JsonCollectionsKtTest {
    
    @Test
    fun testArrayJson() {
        println("ArrayJson")
        val ints = Array.of(1, 2, 3)
        val si = prettyGson.toJson(ints.json())
        val di = prettyGson.fromJson<List<Int>>(si).array()

        assertThat(di.size()).isEqualTo(3)
        assertThat(di[0]).isEqualTo(ints[0])
        assertThat(di[1]).isEqualTo(ints[1])
        assertThat(di[2]).isEqualTo(ints[2])


        val points = Array.of(Point.x(1.0), Point.x(2.0), Point.x(3.0))
        val sp = prettyGson.toJson(points.json { it.json() })
        val dp = prettyGson.fromJson<List<PointJson>>(sp).array { it.point() }

        assertThat(dp.size()).isEqualTo(3)
        pointAssertThat(dp[0]).isEqualToPoint(points[0])
        pointAssertThat(dp[1]).isEqualToPoint(points[1])
        pointAssertThat(dp[2]).isEqualToPoint(points[2])
    }

    @Test
    fun testMapJson() {
        println("MapJson")
        val str2int = HashMap.ofEntries(Tuple2("A", 1), Tuple2("B", 2), Tuple2("C", 3))
        val ssi = prettyGson.toJson(str2int.json())
        val dsi = prettyGson.fromJson<List<KeyValueJson<String, Int>>>(ssi).hashMap()

        assertThat(dsi.size()).isEqualTo(3)
        assertThat(dsi["A"].get()).isEqualTo(str2int["A"].get())
        assertThat(dsi["B"].get()).isEqualTo(str2int["B"].get())
        assertThat(dsi["C"].get()).isEqualTo(str2int["C"].get())


        val str2point = HashMap.ofEntries(
                Tuple2("A", Point.x(1.0)), Tuple2("B", Point.x(2.0)), Tuple2("C", Point.x(3.0)))
        val ssp = prettyGson.toJson(str2point.jsonValue { it.json() })
        val dsp = prettyGson.fromJson<List<KeyValueJson<String, PointJson>>>(ssp).hashMapValue { it.point() }

        assertThat(dsp.size()).isEqualTo(3)
        assertThat(dsp["A"].get()).isEqualTo(str2point["A"].get())
        assertThat(dsp["B"].get()).isEqualTo(str2point["B"].get())
        assertThat(dsp["C"].get()).isEqualTo(str2point["C"].get())

    }

    @Test
    fun testOptionJson() {
        println("OptionJson")
        val noneint = Option.`when`(false, 1)
        val sni = prettyGson.toJson(noneint.json())
        val dni = prettyGson.fromJson<OptionJson<Int>>(sni).option()

        assertThat(dni.isEmpty).isTrue()


        val someint = Option.`when`(true, 1)
        val ssi = prettyGson.toJson(someint.json { it })
        val dsi = prettyGson.fromJson<OptionJson<Int>>(ssi).option()

        assertThat(dsi.get()).isEqualTo(someint.get())


        val nonepoint = Option.`when`(false, Point.x(1.0))
        val snp= prettyGson.toJson(nonepoint.json { it.json() })
        val dnp= prettyGson.fromJson<OptionJson<PointJson>>(snp).option { it.point() }

        assertThat(dnp.isEmpty).isTrue()


        val somepoint = Option.`when`(true, Point.x(1.0))
        val ssp = prettyGson.toJson(somepoint.json { it.json() })
        val dsp = prettyGson.fromJson<OptionJson<PointJson>>(ssp).option { it.point() }

        pointAssertThat(dsp.get()).isEqualToPoint(somepoint.get())
    }

}