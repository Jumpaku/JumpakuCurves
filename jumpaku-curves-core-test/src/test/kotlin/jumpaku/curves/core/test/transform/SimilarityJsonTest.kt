package jumpaku.curves.core.test.transform

import jumpaku.commons.json.parseJson
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.transform.SimilarityJson
import jumpaku.curves.core.transform.TransformJson
import jumpaku.curves.core.transform.similarity
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class SimilarityJsonTest {

    val r = Rotate.of(Vector(1.0, 1.0), Vector(0.0, 1.0)).similarity()

    val p = Point(2.0, 2.0, 2.0)
    val o = Point(0.0, 2.0, 2.0)

    @Test
    fun testSimilarityJson() {
        println("SimilarityJson")
        val e = r.at(o)
        val a = SimilarityJson.toJsonStr(r.at(o)).parseJson().let { TransformJson.fromJson(it) }
        Assert.assertThat(a(p), Matchers.`is`(closeTo(e(p))))
    }
}