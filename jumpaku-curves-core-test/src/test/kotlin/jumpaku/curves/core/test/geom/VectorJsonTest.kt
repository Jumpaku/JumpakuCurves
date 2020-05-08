package jumpaku.curves.core.test.geom

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.geom.VectorJson
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class VectorJsonTest {

    val v = Vector(1.0, -2.0, 3.0)

    @Test
    fun testVectorJson() {
        println("VectorJson")
        Assert.assertThat(VectorJson.toJsonStr(v).parseJson().let { VectorJson.fromJson(it) }, Matchers.`is`(closeTo(v)))
    }

}