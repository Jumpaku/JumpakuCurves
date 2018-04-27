package jumpaku.fsc.test.blend

import jumpaku.core.json.parseJson
import jumpaku.fsc.blend.BlendResult
import org.junit.Test
import java.nio.file.Paths

class BlendResultTest {

    val path = Paths.get("./src/test/resources/jumpaku/fsc/test/blend/")

    @Test
    fun testToString() {
        println("ToString")
        for (i in 0..4) {
            val e = path.resolve("BlendResult$i.json").parseJson().flatMap { BlendResult.fromJson(it) }.get()
            val a = e.toJson().let { BlendResult.fromJson(it) }.get()
            blendResultAssertThat(a).isEqualToBlendResult(e)
        }
    }

}