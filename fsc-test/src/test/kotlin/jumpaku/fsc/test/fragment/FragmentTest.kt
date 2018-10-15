package jumpaku.fsc.test.fragment

import jumpaku.core.curve.Interval
import jumpaku.core.json.parseJson
import jumpaku.fsc.fragment.Fragment
import org.junit.Test

class FragmentTest {

    val f = Fragment(Interval(0.5, 2.3), Fragment.Type.Move)

    @Test
    fun testToString() {
        println("ToString")
        f.toString().parseJson().tryFlatMap { Fragment.fromJson(it) }.orThrow().shouldEqualToFragment(f)
    }
}