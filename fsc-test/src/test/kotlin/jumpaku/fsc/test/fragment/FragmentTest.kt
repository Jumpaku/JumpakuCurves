package jumpaku.fsc.test.fragment

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.fsc.fragment.Fragment
import jumpaku.fsc.fragment.Fragmenter
import jumpaku.fsc.fragment.TruthValueThreshold
import org.junit.Test
import java.io.File

class FragmentTest {

    @Test
    fun toStringTest() {
        println("ToString")
        val fscData = File("./src/test/resources/jumpaku/fsc/test/fragment/FragmenterTestFsc0.json")
        val fsc = fscData.parseJson().flatMap { BSpline.fromJson(it) }.get()
        val result = Fragmenter(TruthValueThreshold(0.4, 0.6), 4, 0.1).fragment(fsc)
        val fragment = result.fragments.head()
        fragmentAssertThat(fragment.toString().parseJson().flatMap { Fragment.fromJson(it) }.get()).isEqualToFragment(fragment)
    }
}