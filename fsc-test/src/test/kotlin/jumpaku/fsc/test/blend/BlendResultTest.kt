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
            val e = resourceText("BlendResult$i.json").parseJson().flatMap { BlendResult.fromJson(it) }.get()
            val a = e.toJson().let { BlendResult.fromJson(it) }.get()
            a.shouldEqualToBlendResult(e)
        }
    }

}