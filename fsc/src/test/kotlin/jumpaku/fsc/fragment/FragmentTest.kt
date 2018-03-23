package jumpaku.fsc.fragment

import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.json.parseJson
import org.junit.Test
import java.io.File

class FragmentTest {

    @Test
    fun toStringTest() {
        println("ToString")
        val fscData = File("./src/test/resources/jumpaku/fsc/fragment/FragmenterTestFsc0.json")
        val fsc = fscData.readText().parseJson().get().bSpline
        val result = Fragmenter(TruthValueThreshold(0.4, 0.6), 4, 0.1).fragment(fsc)
        val fragment = result.fragments.head()
        fragmentAssertThat(fragment.toString().parseJson().get().fragment).isEqualToFragment(fragment)
    }
}