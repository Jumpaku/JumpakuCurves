package jumpaku.fsc.oldtest.fragment

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.fragment.FragmentResult
import jumpaku.fsc.fragment.Fragmenter
import jumpaku.fsc.fragment.TruthValueThreshold
import jumpaku.fsc.oldtest.fragment.fragmentAssertThat

import org.junit.Test
import java.io.File

class FragmentResultTest {

    @Test
    fun toStringTest() {
        println("ToString")
        val fscData = File("./src/test/resources/jumpaku/fsc/test/fragment/FragmenterTestFsc0.json")
        val fsc = fscData.parseJson().flatMap { BSpline.fromJson(it) }.get()
        val result = Fragmenter(TruthValueThreshold(0.4, 0.6), 4, 0.1).fragment(fsc)
        val jsonResult = result.toString().parseJson().flatMap { FragmentResult.fromJson(it) }.get()

        jsonResult.fragments.zip(result.fragments).forEach { (a, e) ->
            fragmentAssertThat(a).isEqualToFragment(e)
        }
    }
}