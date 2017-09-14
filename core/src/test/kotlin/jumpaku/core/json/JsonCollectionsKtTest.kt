package jumpaku.core.json

import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonNull
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import io.vavr.Tuple2
import io.vavr.collection.HashMap
import io.vavr.control.Option
import jumpaku.core.affine.Point
import jumpaku.core.affine.point
import jumpaku.core.affine.pointAssertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class JsonCollectionsKtTest {
/*
    @Test
    fun testArrayJson() {
        println("ArrayJson")
        val ints = Array.of(1, 2, 3)
        val si = ints.toJson { it.toJson() }.toString()
        val di = si.parseToJson().get().array { it.int }

        assertThat(di.size()).isEqualTo(3)
        assertThat(di[0]).isEqualTo(ints[0])
        assertThat(di[1]).isEqualTo(ints[1])
        assertThat(di[2]).isEqualTo(ints[2])


        val points = Array.of(Point.x(1.0), Point.x(2.0), Point.x(3.0))
        val sp = points.toJson { it.toJson() }.toString()
        val dp = sp.parseToJson().get().array { it.point }

        assertThat(dp.size()).isEqualTo(3)
        pointAssertThat(dp[0]).isEqualToPoint(points[0])
        pointAssertThat(dp[1]).isEqualToPoint(points[1])
        pointAssertThat(dp[2]).isEqualToPoint(points[2])
    }
*/
    @Test
    fun testMapJson() {
        println("MapJson")
        val str2int = HashMap.ofEntries(Tuple2("A", 1), Tuple2("B", 2), Tuple2("C", 3))
        val ssi = jsonMap(str2int.map { k, v -> Tuple2(k.toJson(), v.toJson()) }).toString()
        val dsi = ssi.parseToJson().get().hashMap.map { k, v -> Tuple2(k.string, v.int) }
        assertThat(dsi.size()).isEqualTo(3)
        assertThat(dsi["A"].get()).isEqualTo(str2int["A"].get())
        assertThat(dsi["B"].get()).isEqualTo(str2int["B"].get())
        assertThat(dsi["C"].get()).isEqualTo(str2int["C"].get())

        val str2point = HashMap.ofEntries(
                Tuple2("A", Point.x(1.0)), Tuple2("B", Point.x(2.0)), Tuple2("C", Point.x(3.0)))
        val ssp = jsonMap(str2point.map { k, v -> Tuple2(k.toJson(), v.toJson()) }).toString()
        val dsp = ssp.parseToJson().get().hashMap.map { k, v -> Tuple2(k.string, v.point) }
        assertThat(dsp.size()).isEqualTo(3)
        assertThat(dsp["A"].get()).isEqualTo(str2point["A"].get())
        assertThat(dsp["B"].get()).isEqualTo(str2point["B"].get())
        assertThat(dsp["C"].get()).isEqualTo(str2point["C"].get())
    }

    @Test
    fun testOptionJson() {
        println("OptionJson")
        val noneint = Option.`when`(false, 1)
        val sni = jsonOption(noneint.map { it.toJson() }).toString()
        val dni = sni.parseToJson().get().option.map { it.int }
        assertThat(dni.isEmpty).isTrue()

        val someint = Option.`when`(true, 1)
        val ssi = jsonOption(someint.map { it.toJson() }).toString()
        val dsi = ssi.parseToJson().get().option.map { it.int }
        assertThat(dsi.get()).isEqualTo(someint.get())

        val nonepoint = Option.`when`(false, Point.x(1.0))
        val snp= jsonOption(nonepoint.map { it.toJson() }).toString()
        val dnp= snp.parseToJson().get().option.map { it.point }
        assertThat(dnp.isEmpty).isTrue()

        val somepoint = Option.`when`(true, Point.x(1.0))
        val ssp = jsonOption(somepoint.map { it.toJson() }).toString()
        val dsp = ssp.parseToJson().get().option.map { it.point }
        pointAssertThat(dsp.get()).isEqualToPoint(somepoint.get())
    }

}