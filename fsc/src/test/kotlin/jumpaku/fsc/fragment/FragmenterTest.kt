package jumpaku.fsc.fragment

import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.curve.bspline.bSplineAssertThat
import jumpaku.core.json.parseToJson
import org.junit.Test
import java.nio.file.Paths

class FragmenterTest {

    private val threshold = TruthValueThreshold(0.4, 0.6)
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