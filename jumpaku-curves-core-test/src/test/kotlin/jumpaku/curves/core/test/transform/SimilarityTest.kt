package jumpaku.curves.core.test.transform

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.*
import org.apache.commons.math3.linear.MatrixUtils
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import kotlin.math.sqrt

class SimilarityTest {

    val r2 = sqrt(2.0)

    val r = Rotate.of(Vector(1.0, 1.0), Vector(0.0, 1.0))
    val t = Translate(1.0, 2.0, -3.0)
    val s = UniformlyScale(2.0)

    val p = Point(2.0, 2.0, 2.0)
    val o = Point(0.0, 2.0, 2.0)

    @Test
    fun testIsSimilarity() {
        val a0 = Similarity.isSimilarity(
            Transform.ofMatrix(
                MatrixUtils.createRealMatrix(
                    arrayOf(
                        doubleArrayOf(1.0, 2.0, 3.0, 4.0),
                        doubleArrayOf(1.0, 2.0, 3.0, 4.0),
                        doubleArrayOf(1.0, 2.0, 3.0, 4.0),
                        doubleArrayOf(0.0, 0.0, 0.0, 1.0),
                    )
                )
            ), 1e-10
        )
        assertThat(a0, `is`(false))

        val a1 = Similarity.isSimilarity(
            Transform.ofMatrix(
                MatrixUtils.createRealMatrix(
                    arrayOf(
                        doubleArrayOf(1.0, 3.0, 2.0, 4.0),
                        doubleArrayOf(2.0, 0.0, 1.0, 5.0),
                        doubleArrayOf(2.0, 0.0, 2.0, 6.0),
                        doubleArrayOf(0.0, 0.0, 0.0, 1.0),
                    )
                )
            ), 1e-10
        )
        assertThat(a1, `is`(true))
    }

    @Test
    fun testSimilarity_Rotate() {
        println("Similarity_Rotate")
        val a = r.similarity()
        val e = r
        assertThat(a(p), `is`(closeTo(e(p))))
    }

    @Test
    fun testSimilarity_Translate() {
        println("Similarity_Translate")
        val a = t.similarity()
        val e = t
        assertThat(a(p), `is`(closeTo(e(p))))
    }

    @Test
    fun testSimilarity_Scale() {
        println("Similarity_Scale")
        val a = s.similarity()
        val e = s
        assertThat(a(p), `is`(closeTo(e(p))))
    }

    @Test
    fun testAndThenSimilarity() {
        println("AndThenSimilarity")
        val f = Similarity().andThenSimilarly(r).andThenSimilarly(t).andThenSimilarly(s)
        assertThat(f(p), `is`(closeTo(Point(2.0, r2 * 4 + 4, -2.0))))
    }

    @Test
    fun testAt() {
        println("At")
        assertThat(r.similarity().at(o)(p), `is`(closeTo(Point(r2, 2 + r2, 2.0))))
        assertThat(t.similarity().at(o)(p), `is`(closeTo(Point(3.0, 4.0, -1.0))))
        assertThat(s.similarity().at(o)(p), `is`(closeTo(Point(4.0, 2.0, 2.0))))
    }
}

