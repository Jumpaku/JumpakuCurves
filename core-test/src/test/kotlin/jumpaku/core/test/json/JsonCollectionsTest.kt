package jumpaku.core.test.json

import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import io.vavr.Tuple2
import io.vavr.collection.HashMap
import io.vavr.control.Option
import jumpaku.core.affine.Point
import jumpaku.core.json.*
import jumpaku.core.test.affine.shouldEqualToPoint
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqualTo
import org.junit.Test

class JsonCollectionsKtTest {

    @Test
    fun testMapJson() {
        println("MapJson")
        val str2int = HashMap.ofEntries(Tuple2("A", 1), Tuple2("B", 2), Tuple2("C", 3))
        val ssi = jsonMap(str2int.map { k, v -> Tuple2(k.toJson(), v.toJson()) }).toString()
        val dsi = ssi.parseJson().get().hashMap.map { k, v -> Tuple2(k.string, v.int) }
        dsi.size().shouldEqualTo(3)
        dsi["A"].get().shouldEqualTo(str2int["A"].get())
        dsi["B"].get().shouldEqualTo(str2int["B"].get())
        dsi["C"].get().shouldEqualTo(str2int["C"].get())

        val str2point = HashMap.ofEntries(
                Tuple2("A", Point.x(1.0)), Tuple2("B", Point.x(2.0)), Tuple2("C", Point.x(3.0)))
        val ssp = jsonMap(str2point.map { k, v -> Tuple2(k.toJson(), v.toJson()) }).toString()
        val dsp = ssp.parseJson().get().hashMap.map { k, v -> Tuple2(k.string, Point.fromJson(v).get()) }
        dsp.size().shouldEqualTo(3)
        dsp["A"].get().shouldEqualToPoint(str2point["A"].get())
        dsp["B"].get().shouldEqualToPoint(str2point["B"].get())
        dsp["C"].get().shouldEqualToPoint(str2point["C"].get())
    }

    @Test
    fun testOptionJson() {
        println("OptionJson")
        val noneint = Option.`when`(false, 1)
        val sni = jsonOption(noneint.map { it.toJson() }).toString()
        val dni = sni.parseJson().get().option.map { it.int }
        dni.isEmpty.shouldBeTrue()

        val someint = Option.`when`(true, 1)
        val ssi = jsonOption(someint.map { it.toJson() }).toString()
        val dsi = ssi.parseJson().get().option.map { it.int }
        dsi.get().shouldEqualTo(someint.get())

        val nonepoint = Option.`when`(false, Point.x(1.0))
        val snp= jsonOption(nonepoint.map { it.toJson() }).toString()
        val dnp= snp.parseJson().get().option.flatMap { Point.fromJson(it) }
        dnp.isEmpty.shouldBeTrue()

        val somepoint = Option.`when`(true, Point.x(1.0))
        val ssp = jsonOption(somepoint.map { it.toJson() }).toString()
        val dsp = ssp.parseJson().get().option.flatMap { Point.fromJson(it) }
        dsp.get().shouldEqualToPoint(somepoint.get())
    }

}