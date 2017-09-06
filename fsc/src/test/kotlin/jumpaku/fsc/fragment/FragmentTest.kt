package jumpaku.fsc.fragment

import com.github.salomonbrys.kotson.fromJson
import jumpaku.core.curve.bspline.BSplineJson
import jumpaku.core.fuzzy.TruthValue
import jumpaku.core.json.prettyGson
import org.junit.Test
import java.io.File

class FragmentTest {

    @Test
    fun toStringTest() {
        println("ToString")
        val fscData = File("./src/test/resources/jumpaku/fsc/fragment/FragmenterTestFsc0.json")
        val fsc = prettyGson.fromJson<BSplineJson>(fscData.readText()).bSpline()
        val result = Fragmenter(TruthValue(0.4, 0.6), 4, 0.1).fragment(fsc)
        val fragment = result.fragments.head()
        fragmentAssertThat(fragment.json().fragment()).isEqualToFragment(fragment)
    }
}