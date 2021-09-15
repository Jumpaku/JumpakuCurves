package jumpaku.curves.core.test.transform

import jumpaku.commons.math.test.closeTo
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.test.geom.closeTo
import jumpaku.curves.core.transform.*
import org.apache.commons.math3.linear.MatrixUtils
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import kotlin.math.sqrt

class SimilarityTransformTest {

    val r2 = sqrt(2.0)

    val r_affine = Rotate.of(Vector(1.0, 1.0), Vector(0.0, 1.0))
    val t_affine = Translate(1.0, 2.0, -3.0)
    val s_affine = UniformlyScale(2.0)

    val r = r_affine.asSimilarity()
    val t = t_affine.asSimilarity()
    val s = s_affine.asSimilarity()

    val p = Point(2.0, 2.0, 2.0, 5.0)

    @Test
    fun testIsSimilarity() {
        val a0 = SimilarityTransform.isSimilarity(
            AffineTransform.ofMatrix(
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

        val a1 = SimilarityTransform.isSimilarity(
            AffineTransform.ofMatrix(
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

        assertThat(SimilarityTransform.isSimilarity(r_affine), `is`(true))
        assertThat(SimilarityTransform.isSimilarity(t_affine), `is`(true))
        assertThat(SimilarityTransform.isSimilarity(s_affine), `is`(true))
        assertThat(SimilarityTransform.isSimilarity(AffineTransform.Identity), `is`(true))
    }

    @Test
    fun testAsSimilarity_Rotate() {
        println("AsSimilarity_Rotate")
        val a = r_affine.asSimilarity()
        val e = r_affine
        assertThat(a(p), `is`(closeTo(e(p).copy(r = p.r))))
    }

    @Test
    fun testAsSimilarity_Translate() {
        println("AsSimilarity_Translate")
        val a = t_affine.asSimilarity()
        val e = t_affine
        assertThat(a(p), `is`(closeTo(e(p).copy(r = p.r))))
    }

    @Test
    fun testAsSimilarity_Scale() {
        println("AsSimilarity_Scale")
        val a = s_affine.asSimilarity()
        val e = s_affine
        assertThat(a(p), `is`(closeTo(e(p).copy(r = p.r * s_affine.scale))))
    }

    @Test
    fun testInvert() {
        println("Invert")
        assertThat(r.invert().orThrow()(p), `is`(closeTo(Point(2 * r2, 0.0, 2.0, p.r))))
        assertThat(t.invert().orThrow()(p), `is`(closeTo(Point(1.0, 0.0, 5.0, p.r))))
        assertThat(s.invert().orThrow()(p), `is`(closeTo(Point(1.0, 1.0, 1.0, p.r / s_affine.scale))))
    }

    @Test
    fun testIdentity() {
        println("Identity")
        assertThat(SimilarityTransform.Identity(p), `is`(closeTo(p)))
        assertThat(SimilarityTransform.Identity.invert().orThrow()(p), `is`(closeTo(p)))
    }

    @Test
    fun testAndThen() {
        println("AndThen")
        val f = SimilarityTransform().andThen(r).andThen(t).andThen(s)
        assertThat(f(p), `is`(closeTo(Point(2.0, r2 * 4 + 4, -2.0, p.r * s_affine.scale))))
    }

    @Test
    fun testAt() {
        println("At")
        val o = Point(0.0, 2.0, 2.0)
        assertThat(r.at(o)(p), `is`(closeTo(Point(r2, 2 + r2, 2.0, p.r))))
        assertThat(t.at(o)(p), `is`(closeTo(Point(3.0, 4.0, -1.0, p.r))))
        assertThat(s.at(o)(p), `is`(closeTo(Point(4.0, 2.0, 2.0, p.r * s_affine.scale))))
    }

    @Test
    fun testAsAffine() {
        println("AsAffine")
        assertThat(r.asAffine()(p), `is`(closeTo(r_affine(p))))
        assertThat(t.asAffine()(p), `is`(closeTo(t_affine(p))))
        assertThat(s.asAffine()(p), `is`(closeTo(s_affine(p))))
    }

    @Test
    fun testScale() {
        println("Scale")
        assertThat(r.scale(), `is`(closeTo(1.0)))
        assertThat(t.scale(), `is`(closeTo(1.0)))
        assertThat(s.scale(), `is`(closeTo(2.0)))
    }
}

