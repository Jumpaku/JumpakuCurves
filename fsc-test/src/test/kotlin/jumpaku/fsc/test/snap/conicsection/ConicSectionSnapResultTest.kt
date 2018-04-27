package jumpaku.fsc.test.snap.conicsection

import jumpaku.core.json.parseJson
import jumpaku.fsc.snap.conicsection.ConicSectionSnapResult
import org.junit.Test
import java.nio.file.Paths


class ConicSectionSnapResultTest {

    val path = Paths.get("./src/test/resources/jumpaku/fsc/test/snap/conicsection/")

    @Test
    fun testToString() {
        println("ToString")
        for (i in 0..4) {
            val e = path.resolve("ConicSectionSnapResult$i.json").parseJson().flatMap { ConicSectionSnapResult.fromJson(it) }.get()
            val a = e.toString().parseJson().flatMap { ConicSectionSnapResult.fromJson(it) }.get()
            conicSectionSnapResultAssertThat(a).`as`("$i").isEqualToConicSectionSnapResult(e)
        }
    }

}