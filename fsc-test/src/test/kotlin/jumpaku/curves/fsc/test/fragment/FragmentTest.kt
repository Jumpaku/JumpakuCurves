package jumpaku.curves.fsc.test.fragment

import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.json.parseJson
import jumpaku.curves.fsc.fragment.Fragment
import org.junit.Test

class FragmentTest {

    val f = Fragment(Interval(0.5, 2.3), Fragment.Type.Move)

    @Test
    fun testToString() {
        println("ToString")
        f.toString().parseJson().tryMap { Fragment.fromJson(it) }.orThrow().shouldEqualToFragment(f)
    }
}