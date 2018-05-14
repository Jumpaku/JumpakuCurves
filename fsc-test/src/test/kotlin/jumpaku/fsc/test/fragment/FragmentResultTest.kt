package jumpaku.fsc.test.fragment

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.fsc.fragment.FragmentResult
import jumpaku.fsc.fragment.Fragmenter
import jumpaku.fsc.fragment.TruthValueThreshold
import org.junit.Test

class FragmentResultTest {

    val urlString = "/jumpaku/fsc/test/fragment/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val fragmenter = Fragmenter(TruthValueThreshold(0.4, 0.6), 4, 0.1)

    @Test
    fun toStringTest() {
        println("ToString")
        val fsc = resourceText("FragmenterTestFsc0.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
        val result = fragmenter.fragment(fsc)
        result.toString().parseJson().flatMap { FragmentResult.fromJson(it) }.get().shouldEqualToFragmentResult(result)
    }
}