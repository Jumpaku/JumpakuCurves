package jumpaku.core.test.affine

import io.vavr.API
import jumpaku.core.affine.*
import jumpaku.core.json.parseJson
import org.junit.Test
import kotlin.math.sqrt

class AffineTest {

    @Test
    fun testInvoke() {
        println("Invoke")
        val t = translation(Vector(2.3, -5.4, -0.5)).invoke(Point.xyz(3.3, -2.4, -1.0))
        t.shouldEqualToPoint(Point.xyz(5.6, -7.8, -1.5))
        val r = rotation(Vector(1.0, 1.0, 1.0), -Math.PI * 4.0 / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        r.shouldEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val s = scaling(0.5, 0.5, 2.0).invoke(Point.xyz(3.0, -2.0, -1.0))
        s.shouldEqualToPoint(Point.xyz(1.5, -1.0, -2.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        val t = translation(Vector(2.3, -5.4, -0.5)).toString().parseJson().flatMap { Affine.fromJson(it) }.get()
                .invoke(Point.xyz(3.3, -2.4, -1.0))
        t.shouldEqualToPoint(Point.xyz(5.6, -7.8, -1.5))
        val r = rotation(Vector(1.0, 1.0, 1.0), -Math.PI * 4.0 / 3.0).toString().parseJson().flatMap { Affine.fromJson(it) }.get()
                .invoke(Point.xyz(1.0, 1.0, -1.0))
        r.shouldEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val s = scaling(0.5, 0.5, 2.0).toString().parseJson().flatMap { Affine.fromJson(it) }.get()
                .invoke(Point.xyz(3.0, -2.0, -1.0))
        s.shouldEqualToPoint(Point.xyz(1.5, -1.0, -2.0))
    }

    @Test
    fun testInvert() {
        println("Invert")
        val ti = translation(Vector(-2.3, 5.4, 0.5)).invert().get().invoke(Point.xyz(3.3, -2.4, -1.0))
        ti.shouldEqualToPoint(Point.xyz(5.6, -7.8, -1.5))
        val ri = rotation(Vector(1.0, 1.0, 1.0), Math.PI * 4.0 / 3.0).invert().get().invoke(Point.xyz(1.0, 1.0, -1.0))
        ri.shouldEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val si = scaling(2.0, 2.0, 0.5).invert().get().invoke(Point.xyz(3.0, -2.0, -1.0))
        si.shouldEqualToPoint(Point.xyz(1.5, -1.0, -2.0))
    }

    @Test
    fun testConcatenate() {
        println("Concatenate")
        val c = identity
                .andThen(translation(Vector(-2.0, 5.0, 1.0)))
                .andThen(rotation(Vector(1.0, 1.0, 1.0), Math.PI * 2.0 / 3.0))
                .invoke(Point.xyz(3.0, -2.0, -1.0))
        c.shouldEqualToPoint(Point.xyz(0.0, 1.0, 3.0))
    }

    @Test
    fun testTransformAt() {
        println("TransformAt")
        val t = transformationAt(Point.xyz(1.0, 2.0, 3.0),
                translation(Vector(-2.3, 5.4, 0.5))).invoke(Point.xyz(3.3, -2.4, -1.0))
        t.shouldEqualToPoint(Point.xyz(1.0, 3.0, -0.5))
        val r = transformationAt(Point.xyz(1.0, -1.0, 1.0),
                rotation(Vector(1.0, 1.0, 1.0), Math.PI / 3.0)).invoke(Point.xyz(1.0, 1.0, -1.0))
        r.shouldEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val s = transformationAt(Point.xyz(1.0, 1.0, 1.0),
                scaling(2.3, 5.4, 0.5)).invoke(Point.xyz(3.0, -2.0, -1.0))
        s.shouldEqualToPoint(Point.xyz(5.6, -15.2, 0.0))
    }

    @Test
    fun testSimilarity() {
        println("Similarity")
        val s = similarity(API.Tuple(Point.xyz(1.0, 0.0, 0.0), Point.xyz(0.0, 1.0, 0.0)), API.Tuple(Point.xyz(1.0, 1.0, -1.0), Point.xyz(-1.0, 1.0, 1.0)))
                .get().invoke(Point.xyz(0.0, 0.0, 1.0))
        s.shouldEqualToPoint(Point.xyz(1.0, -1.0, 1.0))
    }

    @Test
    fun testSimilarityWithNormal() {
        println("SimilarityWithNormal")
        val s = similarityWithNormal(
                API.Tuple(Point.xyz(1.0, 0.0, 0.0), Point.xyz(0.0, 1.0, 0.0), Vector(0.0, 0.0, 1.0)),
                API.Tuple(Point.xyz(1.0, 1.0, -1.0), Point.xyz(-1.0, 1.0, 1.0), Vector(0.0, -1.0, 0.0)))
                .get().invoke(Point.xyz(1.0, 1.0, 0.0))
        s.shouldEqualToPoint(Point.xyz(1.0, 1.0, 1.0))
    }

    @Test
    fun testCalibrate() {
        println("calibrate")
        val c = calibrate(
                API.Tuple(Point.xyz(1.0, 0.0, 0.0), Point.xyz(0.0, 1.0, 0.0), Point.xyz(0.0, 0.0, 1.0), Point.xyz(-1.0, -1.0, -1.0)),
                API.Tuple(Point.xyz(1.0, -1.0, 1.0), Point.xyz(1.0, 1.0, -1.0), Point.xyz(-1.0, 1.0, 1.0), Point.xyz(1.0, 1.0, 1.0)))
        val c0 = c.get().invoke(Point.xyz(1.0, 0.0, 0.0))
        c0.shouldEqualToPoint(Point.xyz(1.0, -1.0, 1.0))
        val c1 = c.get().invoke(Point.xyz(0.0, 1.0, 0.0))
        c1.shouldEqualToPoint(Point.xyz(1.0, 1.0, -1.0))
        val c2 = c.get().invoke(Point.xyz(0.0, 0.0, 1.0))
        c2.shouldEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val c3 = c.get().invoke(Point.xyz(-1.0, -1.0, -1.0))
        c3.shouldEqualToPoint(Point.xyz(1.0, 1.0, 1.0))
        val r0 = c.get().invoke(Point.xyz(0.0, 0.0, 0.0))
        r0.shouldEqualToPoint(Point.xyz(0.5, 0.5, 0.5))
    }

    @Test
    fun testCalibrateToPlane() {
        println("CalibrateToPlane")
        val c = calibrateToPlane(
                API.Tuple(Point.xyz(1.0, 0.0, 0.0), Point.xyz(0.0, 1.0, 0.0), Point.xyz(0.0, 0.0, 1.0)),
                API.Tuple(Point.xyz(1.0, -1.0, 1.0), Point.xyz(1.0, 1.0, -1.0), Point.xyz(-1.0, 1.0, 1.0)))
        val c0 = c.get().invoke(Point.xyz(1.0, 0.0, 0.0))
        c0.shouldEqualToPoint(Point.xyz(1.0, -1.0, 1.0))
        val c1 = c.get().invoke(Point.xyz(0.0, 1.0, 0.0))
        c1.shouldEqualToPoint(Point.xyz(1.0, 1.0, -1.0))
        val c2 = c.get().invoke(Point.xyz(0.0, 0.0, 1.0))
        c2.shouldEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val c3 = c.get().invoke(Point.xyz(1 + sqrt(3.0), sqrt(3.0), sqrt(3.0)))
        c3.shouldEqualToPoint(Point.xyz(1.0, -1.0, 1.0))
        val r0 = c.get().invoke(Point.xyz(1.0, 1.0, 2.0))
        r0.shouldEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
    }

    @Test
    fun testScale() {
        println("Scale")
        val s0 = identity.andScale(2.0, 3.0, 4.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        s0.shouldEqualToPoint(Point.xyz(4.0, -6.0, -4.0))

        val s1 = identity.andScale(2.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        s1.shouldEqualToPoint(Point.xyz(4.0, -4.0, -2.0))

        val s2 = identity.andScaleAt(Point.xyz(1.0, 1.0, 1.0), 2.0, 3.0, 4.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        s2.shouldEqualToPoint(Point.xyz(3.0, -8.0, -7.0))

        val s3 = identity.andScaleAt(Point.xyz(1.0, 1.0, 1.0), 3.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        s3.shouldEqualToPoint(Point.xyz(4.0, -8.0, -5.0))
    }

    @Test
    fun testRotate() {
        println("Rotate")
        val e = Point.xyz(-1.0, 1.0, 1.0)
        val r0 = identity.andRotate(Vector(1.0, 1.0, 1.0), Math.PI * 2.0 / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        r0.shouldEqualToPoint(e)
        val r1 = identity.andRotate(Point.xyz(0.0, 0.0, -1.0), Point.xyz(1.0, 1.0, 0.0), Math.PI * 2.0 / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        r1.shouldEqualToPoint(e)
        val r2 = identity.andRotateAt(Point.xyz(1.0, 1.0, 1.0), Vector(0.0, 1.0, 0.0), Math.PI / 2).invoke(Point.xyz(1.0, 1.0, -1.0))
        r2.shouldEqualToPoint(e)
        val r3 = identity.andRotate(Vector(0.0, 1.0, -1.0), Vector(-1.0, 1.0, 0.0), Math.PI / 3.0 * 2.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        r3.shouldEqualToPoint(e)
        val r4 = identity.andRotate(Vector(0.0, 1.0, -1.0), Vector(-1.0, 0.0, 1.0)).invoke(Point.xyz(1.0, 1.0, -1.0))
        r4.shouldEqualToPoint(e)
        val r5 = identity.andRotateAt(Point.xyz(1.0, -1.0, 1.0), Vector(0.0, 1.0, -1.0), Vector(-1.0, 0.0, 1.0), Math.PI / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        r5.shouldEqualToPoint(e)
        val r6 = identity.andRotateAt(Point.xyz(1.0, -1.0, 1.0), Vector(0.0, 2.0, -2.0), Vector(-2.0, 2.0, 0.0)).invoke(Point.xyz(1.0, 1.0, -1.0))
        r6.shouldEqualToPoint(e)

    }

    @Test
    fun testTranslate() {
        println("Translate")
        val e = Point.xyz(1.0, 3.0, -0.5)
        val t0 = identity.andTranslate(Vector(-2.3, 5.4, 0.5)).invoke(Point.xyz(3.3, -2.4, -1.0))
        t0.shouldEqualToPoint(e)
        val t1 = identity.andTranslate(-2.3, 5.4, 0.5).invoke(Point.xyz(3.3, -2.4, -1.0))
        t1.shouldEqualToPoint(e)
    }
}