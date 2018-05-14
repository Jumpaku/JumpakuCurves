package jumpaku.fsc.test.snap.conicsection

import jumpaku.core.json.parseJson
import jumpaku.fsc.snap.conicsection.ConicSectionSnapResult
import org.junit.Test

class ConicSectionSnapResultTest {

    val urlString = "/jumpaku/fsc/test/snap/conicsection/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    @Test
    fun testToString() {
        println("ToString")
        for (i in 0..4) {
            val e = resourceText("ConicSectionSnapResult$i.json").parseJson().flatMap { ConicSectionSnapResult.fromJson(it) }.get()
            val a = e.toString().parseJson().flatMap { ConicSectionSnapResult.fromJson(it) }.get()
            a.shouldEqualToConicSectionSnapResult(e)
        }
    }

}