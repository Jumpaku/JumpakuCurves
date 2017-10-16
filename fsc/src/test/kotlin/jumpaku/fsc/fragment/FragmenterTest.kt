package jumpaku.fsc.fragment

import com.github.salomonbrys.kotson.fromJson
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.bspline.BSplineAssert
import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.curve.bspline.bSplineAssertThat
import jumpaku.core.fuzzy.TruthValue
import jumpaku.core.json.parseToJson
import jumpaku.core.json.prettyGson
import org.junit.Test
import org.junit.Assert.*
import java.nio.file.Paths

class FragmenterTest {

    private val threshold = TruthValue(0.4, 0.6)
    private val path = Paths.get("./src/test/resources/jumpaku/fsc/fragment/")

    @Test
    fun fragment() {
        println("Fragment")
        for (i in 0..1) {
            val fscData = path.resolve("FragmenterTestFsc$i.json").toFile()
            val fsc = fscData.readText().parseToJson().get().bSpline
            val result = Fragmenter(threshold, 4, 0.1).fragment(fsc)
            result.fragments.forEachIndexed { index, (interval, _) ->
                val f = fsc.restrict(interval)
                val fData = path.resolve("FragmenterTestData${i}_$index.json").toFile()
                val fFsc = fData.readText().parseToJson().get().bSpline
                bSplineAssertThat(f).isEqualToBSpline(fFsc)
            }
        }
    }

}