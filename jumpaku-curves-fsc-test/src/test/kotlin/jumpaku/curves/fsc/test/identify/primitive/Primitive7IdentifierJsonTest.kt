package jumpaku.curves.fsc.test.identify.primitive

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.identify.primitive.Primitive7Identifier
import jumpaku.curves.fsc.identify.primitive.Primitive7IdentifierJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class Primitive7IdentifierJsonTest {

    val identifier = Primitive7Identifier(nSamples = 25, nFmps = 15)

    @Test
    fun testPrimitive7IdentifierJson() {
        println("Primitive7IdentifierJson")
        val a = Primitive7IdentifierJson.toJsonStr(identifier).parseJson().let { Primitive7IdentifierJson.fromJson(it) }
        Assert.assertThat(a, Matchers.`is`(equalTo(identifier)))
    }
}