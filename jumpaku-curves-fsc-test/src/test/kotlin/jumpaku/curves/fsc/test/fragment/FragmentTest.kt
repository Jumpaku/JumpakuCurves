package jumpaku.curves.fsc.test.fragment

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.fsc.fragment.Fragment
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class FragmentTest {

    val f = Fragment(Interval(0.5, 2.3), Fragment.Type.Move)

    @Test
    fun testToString() {
        println("ToString")
        val a = f.toString().parseJson().tryMap { Fragment.fromJson(it) }.orThrow()
        assertThat(a, `is`(closeTo(f)))
    }
}