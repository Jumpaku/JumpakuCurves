package jumpaku.curves.fsc.test.identify.nquarter

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.identify.nquarter.NQuarterIdentifier
import jumpaku.curves.fsc.identify.nquarter.NQuarterIdentifierJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class NQuarterIdentifierJsonTest {

    val identifier = NQuarterIdentifier(25, 15)

    @Test
    fun testNQuarterIdentifierJson() {
        println("NQuarterIdentifierJson")
        val a = NQuarterIdentifierJson.toJsonStr(identifier).parseJson().let { NQuarterIdentifierJson.fromJson(it) }
        Assert.assertThat(a, Matchers.`is`(equalTo(identifier)))
    }
}