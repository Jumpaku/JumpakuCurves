package jumpaku.fsc.test.fragment

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.core.test.curve.bspline.bSplineAssertThat
import jumpaku.fsc.fragment.Fragmenter
import jumpaku.fsc.fragment.TruthValueThreshold
import org.junit.Test
import java.nio.file.Paths

class FragmenterTest {

    private val threshold = TruthValueThreshold(0.4, 0.6)
    private val path = Paths.get("./src/test/resources/jumpaku/fsc/test/fragment/")

    @Test
    fun fragment() {
        println("Fragment")
        for (i in 0..1) {
            val fscData = path.resolve("FragmenterTestFsc$i.json").toFile()
            val fsc = fscData.parseJson().flatMap { BSpline.fromJson(it) }.get()
            val result = Fragmenter(threshold, 4, 0.1).fragment(fsc)
            result.fragments.forEachIndexed { index, (interval, _) ->
                val f = fsc.restrict(interval)
                val fData = path.resolve("FragmenterTestData${i}_$index.json").toFile()
                val fFsc = fData.parseJson().flatMap { BSpline.fromJson(it) }.get()
                bSplineAssertThat(f).isEqualToBSpline(fFsc)
            }
        }
    }

}