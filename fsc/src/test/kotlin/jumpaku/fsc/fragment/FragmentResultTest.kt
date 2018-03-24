package jumpaku.fsc.fragment

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2

import org.junit.Test
import java.io.File

class FragmentResultTest {

    @Test
    fun toStringTest() {
        println("ToString")
        val fscData = File("./src/test/resources/jumpaku/fsc/fragment/FragmenterTestFsc0.json")
        val fsc = fscData.parseJson().flatMap { BSpline.fromJson(it) }.get()
        val result = Fragmenter(TruthValueThreshold(0.4, 0.6), 4, 0.1).fragment(fsc)
        val jsonResult = result.toString().parseJson().get().fragmentResult

        jsonResult.fragments.zip(result.fragments).map { (a, e) ->
            fragmentAssertThat(a).isEqualToFragment(e)
        }
    }
}