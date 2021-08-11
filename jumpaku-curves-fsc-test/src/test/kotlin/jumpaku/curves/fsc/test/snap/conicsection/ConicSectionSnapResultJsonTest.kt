package jumpaku.curves.fsc.test.snap.conicsection

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.snap.conicsection.ConicSectionSnapResult
import jumpaku.curves.fsc.snap.conicsection.ConicSectionSnapResultJson
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class ConicSectionSnapResultJsonTest {
/*
    val urlString = "/jumpaku/curves/fsc/test/snap/conicsection/"
    init {
        val classPaths = System.getProperty("java.class.path").split(":")
        if(!classPaths.any { it == ("/project/jumpaku-curves-fsc-test/build/resources/test") })
            error("Error!:")
        val t = "${this::class.java.getResource(urlString)}"
        if (!t.endsWith("/jumpaku/curves/fsc/test/snap/conicsection/"))
            error("Error!: ${t}")
        val s = "${this::class.java.getResource(urlString + "snapResult.json")}"
        if (!s.endsWith("/jumpaku/curves/fsc/test/snap/conicsection/snapResult.json"))
            error("Error!: ${s}")
    }
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val r = resourceText("snapResult.json").parseJson().let { ConicSectionSnapResultJson.fromJson(it) }

    @Test
    fun testConicSectionSnapResultJson() {
        println("ConicSectionSnapResultJson")
        assertThat(ConicSectionSnapResultJson.toJsonStr(r).parseJson().let { ConicSectionSnapResultJson.fromJson(it) }, `is`(closeTo(r)))
    }
*/
}
