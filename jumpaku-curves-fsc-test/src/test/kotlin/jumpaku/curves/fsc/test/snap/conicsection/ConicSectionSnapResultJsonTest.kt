package jumpaku.curves.fsc.test.snap.conicsection

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.snap.conicsection.ConicSectionSnapResult
import jumpaku.curves.fsc.snap.conicsection.ConicSectionSnapResultJson
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class ConicSectionSnapResultJsonTest {

    val urlString = "/jumpaku/curves/fsc/test/snap/conicsection/"
    init {
        System.err.println(this::class.java.getResource(urlString + "snapResult.json"))
        System.err.println(this::class.java.getResource(urlString))
    }
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val r = resourceText("snapResult.json").parseJson().let { ConicSectionSnapResultJson.fromJson(it) }

    @Test
    fun testConicSectionSnapResultJson() {
        println("ConicSectionSnapResultJson")
        assertThat(ConicSectionSnapResultJson.toJsonStr(r).parseJson().let { ConicSectionSnapResultJson.fromJson(it) }, `is`(closeTo(r)))
    }

}
