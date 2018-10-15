package jumpaku.fsc.test.snap.conicsection

import jumpaku.core.json.parseJson
import jumpaku.fsc.snap.conicsection.ConicSectionSnapResult
import org.junit.Test

class ConicSectionSnapResultTest {

    val urlString = "/jumpaku/fsc/test/snap/conicsection/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val r = resourceText("snapResultL0.json").parseJson().tryFlatMap { ConicSectionSnapResult.fromJson(it) }.orThrow()

    @Test
    fun testToString() {
        println("ToString")
        for (i in 0..4) {
            r.toString().parseJson().tryFlatMap { ConicSectionSnapResult.fromJson(it) }.orThrow()
                    .shouldEqualToConicSectionSnapResult(r)
        }
    }

}