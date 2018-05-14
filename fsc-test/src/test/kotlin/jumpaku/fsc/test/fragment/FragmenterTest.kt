package jumpaku.fsc.test.fragment

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.core.test.curve.bspline.shouldEqualToBSpline
import jumpaku.fsc.fragment.Fragmenter
import jumpaku.fsc.fragment.TruthValueThreshold
import org.junit.Test

class FragmenterTest {

    private val threshold = TruthValueThreshold(0.4, 0.6)
    val fragmenter = Fragmenter(threshold, 4, 0.1)

    val urlString = "/jumpaku/fsc/test/fragment/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    @Test
    fun fragment() {
        println("Fragment")
        for (i in 0..1) {
            val fsc = resourceText("FragmenterTestFsc$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
            val result = fragmenter.fragment(fsc)
            result.fragments.forEachIndexed { index, (interval, _) ->
                val f = fsc.restrict(interval)
                val fFsc = resourceText("FragmenterTestData${i}_$index.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
                f.shouldEqualToBSpline(fFsc)
            }
        }
    }

}