package jumpaku.fsc.test.fragment

import io.vavr.collection.Array
import jumpaku.core.curve.Interval
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.fsc.fragment.Fragment
import jumpaku.fsc.fragment.FragmentResult
import jumpaku.fsc.fragment.Fragmenter
import jumpaku.fsc.fragment.TruthValueThreshold
import org.junit.Test

class FragmentResultTest {

    val urlString = "/jumpaku/fsc/test/fragment/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val fragmenter = Fragmenter(TruthValueThreshold(0.4, 0.6), 4, 0.1)

    val r = FragmentResult(Array.of(
            Fragment(Interval(0.2, 0.3), Fragment.Type.Stay),
            Fragment(Interval(0.2, 0.3), Fragment.Type.Move)))
    @Test
    fun toStringTest() {
        println("ToString")
        r.toString().parseJson().flatMap { FragmentResult.fromJson(it) }.get().shouldEqualToFragmentResult(r)
    }
}