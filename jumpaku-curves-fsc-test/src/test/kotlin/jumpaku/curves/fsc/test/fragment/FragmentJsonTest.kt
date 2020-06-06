package jumpaku.curves.fsc.test.fragment

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.fsc.fragment.Fragment
import jumpaku.curves.fsc.fragment.FragmentJson
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class FragmentJsonTest {

    val f = Fragment(Interval(0.5, 2.3), Fragment.Type.Move)

    @Test
    fun testFragmentJson() {
        println("FragmentJson")
        val a = FragmentJson.toJsonStr(f).parseJson().let { FragmentJson.fromJson(it) }
        assertThat(a, `is`(closeTo(f)))
    }
}