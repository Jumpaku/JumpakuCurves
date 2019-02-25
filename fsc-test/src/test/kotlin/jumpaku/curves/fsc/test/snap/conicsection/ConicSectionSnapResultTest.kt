package jumpaku.curves.fsc.test.snap.conicsection

import jumpaku.curves.core.json.parseJson
import jumpaku.curves.fsc.snap.conicsection.ConicSectionSnapResult
import org.junit.Test

class ConicSectionSnapResultTest {

    val urlString = "/jumpaku/curves/fsc/test/snap/conicsection/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val r = resourceText("snapResult.json").parseJson().tryMap { ConicSectionSnapResult.fromJson(it) }.orThrow()

    @Test
    fun testToString() {
        println("ToString")
        r.toString().parseJson().tryMap { ConicSectionSnapResult.fromJson(it) }.orThrow().shouldEqualToConicSectionSnapResult(r)
    }

}