package jumpaku.fsc.test.blend

import jumpaku.core.json.parseJson
import jumpaku.fsc.blend.BlendResult
import org.junit.Test

class BlendResultTest {

    val urlString = "/jumpaku/fsc/test/blend/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    @Test
    fun testToString() {
        println("ToString")
        for (i in 0..4) {
            val e = resourceText("BlendResult$i.json").parseJson().tryFlatMap { BlendResult.fromJson(it) }.orThrow()
            val a = e.toJson().let { BlendResult.fromJson(it) }.orThrow()
            a.shouldEqualToBlendResult(e)
        }
    }

}