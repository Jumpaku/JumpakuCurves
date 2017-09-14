package jumpaku.fsc.fragment

import com.github.salomonbrys.kotson.fromJson
import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.fuzzy.TruthValue
import jumpaku.core.json.parseToJson
import jumpaku.core.json.prettyGson
import jumpaku.core.util.component1
import jumpaku.core.util.component2

import org.junit.Test
import java.io.File

class FragmentResultTest {

    @Test
    fun toStringTest() {
        println("ToString")
        val fscData = File("./src/test/resources/jumpaku/fsc/fragment/FragmenterTestFsc0.json")
        val fsc = fscData.readText().parseToJson().get().bSpline
        val result = Fragmenter(TruthValue(0.4, 0.6), 4, 0.1).fragment(fsc)
        val jsonResult = result.toString().parseToJson().get().fragmentResult

        jsonResult.fragments.zip(result.fragments).map { (a, e) ->
            fragmentAssertThat(a).isEqualToFragment(e)
        }
    }
}