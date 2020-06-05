package jumpaku.curves.fsc.test.identify.primitive

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.identify.primitive.Open4Identifier
import jumpaku.curves.fsc.identify.primitive.Open4IdentifierJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class Open4IdentifierJsonTest {

    val identifier = Open4Identifier(nSamples = 25, nFmps = 15)

    @Test
    fun testOpen4IdentifierJson() {
        println("Open4IdentifierJson")
        val a = Open4IdentifierJson.toJsonStr(identifier).parseJson().let { Open4IdentifierJson.fromJson(it) }
        Assert.assertThat(a, Matchers.`is`(equalTo(identifier)))
    }
}