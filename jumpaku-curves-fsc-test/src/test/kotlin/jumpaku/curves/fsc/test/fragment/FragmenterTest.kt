package jumpaku.curves.fsc.test.fragment

import com.github.salomonbrys.kotson.array
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bspline.BSplineJson
import jumpaku.curves.core.test.curve.closeTo
import jumpaku.curves.fsc.fragment.Chunk
import jumpaku.curves.fsc.fragment.FragmentJson
import jumpaku.curves.fsc.fragment.Fragmenter
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class FragmenterTest {

    val threshold = Chunk.Threshold(0.4, 0.6)
    val fragmenter = Fragmenter(threshold, 4, 0.1)

    val urlString = "/jumpaku/curves/fsc/test/fragment/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    @Test
    fun testFragment() {
        println("Fragment")
        for (i in 0..1) {
            val fsc = resourceText("Fsc$i.json").parseJson().let { BSplineJson.fromJson(it) }
            val a = fragmenter.fragment(fsc)
            val e = resourceText("FragmentResult$i.json").parseJson().let { it.array.map { FragmentJson.fromJson(it) } }
            assertThat(a.size, `is`(e.size))
            a.zip(e).forEach { (a, e) ->
                assertThat(a.type, `is`(e.type))
                assertThat(a.interval, `is`(closeTo(e.interval)))
            }
        }
    }
}
