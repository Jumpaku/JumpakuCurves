package jumpaku.fsc.test.fragment

import com.github.salomonbrys.kotson.array
import io.vavr.collection.Array
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.json.parseJson
import jumpaku.curves.core.test.curve.shouldEqualToInterval
import jumpaku.fsc.fragment.Fragment
import jumpaku.fsc.fragment.Fragmenter
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqualTo
import org.junit.Test

class FragmenterTest {

    private val threshold = Fragmenter.Threshold(0.4, 0.6)
    val fragmenter = Fragmenter(threshold, 4, 0.1)

    val urlString = "/jumpaku/fsc/test/fragment/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    @Test
    fun fragment() {
        println("Fragment")
        for (i in 0..1) {
            val fsc = resourceText("Fsc$i.json").parseJson().tryMap { BSpline.fromJson(it) }.orThrow()
            val a = fragmenter.fragment(fsc)
            val e = resourceText("FragmentResult$i.json").parseJson().tryMap { it.array.map { Fragment.fromJson(it) } }.orThrow()
                    .let { Array.ofAll(it) }
            a.size.shouldEqualTo(e.size())
            a.zip(e).forEach { (a, e) ->
                a.type.shouldBe(e.type)
                a.interval.shouldEqualToInterval(e.interval)
            }
        }
    }

}