package jumpaku.fsc.test.fragment

import com.github.salomonbrys.kotson.array
import io.vavr.collection.Array
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.core.test.curve.shouldEqualToInterval
import jumpaku.fsc.fragment.Fragment
import jumpaku.fsc.fragment.Fragmenter
import jumpaku.fsc.fragment.TruthValueThreshold
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqualTo
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
            val fsc = resourceText("Fsc$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
            val a = fragmenter.fragment(fsc)
            val e = resourceText("FragmentResult$i.json").parseJson().map { it.array.flatMap { Fragment.fromJson(it) } }.get()
                    .let { Array.ofAll(it) }
            a.size().shouldEqualTo(e.size())
            a.zip(e).forEach { (a, e) ->
                a.type.shouldBe(e.type)
                a.interval.shouldEqualToInterval(e.interval)
            }
        }
    }

}