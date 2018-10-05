package jumpaku.fsc.test.fragment

import jumpaku.core.curve.Interval
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.fsc.fragment.Fragment
import jumpaku.fsc.fragment.Fragmenter
import jumpaku.fsc.fragment.TruthValueThreshold
import org.junit.Test

class FragmentTest {

    val f = Fragment(Interval(0.5, 2.3), Fragment.Type.Move)

    @Test
    fun testToString() {
        println("ToString")
        f.toString().parseJson().tryFlatMap { Fragment.fromJson(it) }.orThrow().shouldEqualToFragment(f)
    }
}