package jumpaku.fsc.test.fragment

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.fsc.fragment.Fragment
import jumpaku.fsc.fragment.Fragmenter
import jumpaku.fsc.fragment.TruthValueThreshold
import org.junit.Test

class FragmentTest {

    val urlString = "/jumpaku/fsc/test/fragment/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val fsc = resourceText("FragmenterTestFsc0.json").parseJson().flatMap { BSpline.fromJson(it) }.get()

    @Test
    fun testToString() {
        println("ToString")
        val result = Fragmenter(TruthValueThreshold(0.4, 0.6), 4, 0.1).fragment(fsc)
        val fragment = result.fragments.head()
        fragment.toString().parseJson().flatMap { Fragment.fromJson(it) }.get().shouldBeFragment(fragment)
    }
}