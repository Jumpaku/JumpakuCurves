package jumpaku.curves.core.test.json

import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.json.jsonMap
import jumpaku.curves.core.json.map
import jumpaku.curves.core.json.parseJson
import jumpaku.curves.core.test.geom.closeTo
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class JsonMapKtTest {

    @Test
    fun testMapJson() {
        println("MapJson")
        val str2int = mapOf("A" to 1, "B" to 2, "C" to 3)
        val ssi = jsonMap(str2int.map { (k, v) -> k.toJson() to v.toJson() }.toMap()).toString()
        val dsi = ssi.parseJson().orThrow().map.map { (k, v) -> Pair(k.string, v.int) }.toMap()
        assertThat(dsi.size, `is`(3))
        assertThat(dsi["A"]!!, `is`(str2int["A"]!!))
        assertThat(dsi["B"]!!, `is`(str2int["B"]!!))
        assertThat(dsi["C"]!!, `is`(str2int["C"]!!))

        val str2point = mapOf("A" to Point.x(1.0), "B" to Point.x(2.0), "C" to Point.x(3.0))
        val ssp = jsonMap(str2point.map { (k, v) -> k.toJson() to v.toJson() }.toMap()).toString()
        val dsp = ssp.parseJson().orThrow().map.map { (k, v) -> k.string to Point.fromJson(v) }.toMap()
        assertThat(dsp.size, `is`(3))
        assertThat(dsp["A"]!!, `is`(closeTo(str2point["A"]!!)))
        assertThat(dsp["B"]!!, `is`(closeTo(str2point["B"]!!)))
        assertThat(dsp["C"]!!, `is`(closeTo(str2point["C"]!!)))
    }
}