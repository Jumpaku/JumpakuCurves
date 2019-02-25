package jumpaku.curves.core.test.json

import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.json.*
import jumpaku.curves.core.test.geom.shouldEqualToPoint
import org.amshove.kluent.shouldEqualTo
import org.junit.Test

class JsonMapKtTest {

    @Test
    fun testMapJson() {
        println("MapJson")
        val str2int = mapOf("A" to 1, "B" to 2, "C" to 3)
        val ssi = jsonMap(str2int.map { (k, v) -> k.toJson() to v.toJson() }.toMap()).toString()
        val dsi = ssi.parseJson().orThrow().map.map { (k, v) -> Pair(k.string, v.int) }.toMap()
        dsi.size.shouldEqualTo(3)
        dsi["A"]!!.shouldEqualTo(str2int["A"]!!)
        dsi["B"]!!.shouldEqualTo(str2int["B"]!!)
        dsi["C"]!!.shouldEqualTo(str2int["C"]!!)

        val str2point = mapOf("A" to Point.x(1.0), "B" to Point.x(2.0), "C" to Point.x(3.0))
        val ssp = jsonMap(str2point.map { (k, v) -> k.toJson() to v.toJson() }.toMap()).toString()
        val dsp = ssp.parseJson().orThrow().map.map { (k, v) -> k.string to Point.fromJson(v) }.toMap()
        dsp.size.shouldEqualTo(3)
        dsp["A"]!!.shouldEqualToPoint(str2point["A"]!!)
        dsp["B"]!!.shouldEqualToPoint(str2point["B"]!!)
        dsp["C"]!!.shouldEqualToPoint(str2point["C"]!!)
    }
}