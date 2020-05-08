package jumpaku.curves.core.test.transform

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Translate
import jumpaku.curves.core.transform.TranslateJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class TranslateJsonTest {

    val t = Translate(1.0, 2.0, -3.0)
    val p = Point(3.0, 4.0, -5.0)

    @Test
    fun testTranslateJson() {
        println("TranslateJson")
        val a = TranslateJson.toJsonStr(t).parseJson().let { TranslateJson.fromJson(it) }(p)
        Assert.assertThat(a, Matchers.`is`(closeTo(Point(4.0, 6.0, -8.0))))
    }
}