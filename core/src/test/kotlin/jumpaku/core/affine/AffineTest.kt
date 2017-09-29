package jumpaku.core.affine

import io.vavr.API.*
import jumpaku.core.json.parseToJson
import org.junit.Test

/**
 * Created by jumpaku on 2017/05/10.
 */
class AffineTest {

    @Test
    fun testInvoke() {
        println("Invoke")
        val t = translation(Vector(2.3, -5.4, -0.5)).invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(t).isEqualToPoint(Point.xyz(5.6, -7.8, -1.5))
        val r = rotation(Vector(1.0, 1.0, 1.0), -Math.PI * 4.0 / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val s = scaling(0.5, 0.5, 2.0).invoke(Point.xyz(3.0, -2.0, -1.0))
        pointAssertThat(s).isEqualToPoint(Point.xyz(1.5, -1.0, -2.0))
    }

    @Test
    fun testToString() {
        println("ToString")
        val t = translation(Vector(2.3, -5.4, -0.5)).toString().parseToJson().get().affine
                .invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(t).isEqualToPoint(Point.xyz(5.6, -7.8, -1.5))
        val r = rotation(Vector(1.0, 1.0, 1.0), -Math.PI * 4.0 / 3.0).toString().parseToJson().get().affine
                .invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val s = scaling(0.5, 0.5, 2.0).toString().parseToJson().get().affine
                .invoke(Point.xyz(3.0, -2.0, -1.0))
        pointAssertThat(s).isEqualToPoint(Point.xyz(1.5, -1.0, -2.0))
    }

    @Test
    fun testInvert() {
        println("Invert")
        val ti = translation(Vector(-2.3, 5.4, 0.5)).invert().invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(ti).isEqualToPoint(Point.xyz(5.6, -7.8, -1.5))
        val ri = rotation(Vector(1.0, 1.0, 1.0), Math.PI * 4.0 / 3.0).invert().invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(ri).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val si = scaling(2.0, 2.0, 0.5).invert().invoke(Point.xyz(3.0, -2.0, -1.0))
        pointAssertThat(si).isEqualToPoint(Point.xyz(1.5, -1.0, -2.0))
    }

    @Test
    fun testConcatenate() {
        println("Concatenate")
        val c = identity
                .andThen(translation(Vector(-2.0, 5.0, 1.0)))
                .andThen(rotation(Vector(1.0, 1.0, 1.0), Math.PI * 2.0 / 3.0))
                .invoke(Point.xyz(3.0, -2.0, -1.0))
        pointAssertThat(c).isEqualToPoint(Point.xyz(0.0, 1.0, 3.0))
    }

    @Test
    fun testTransformAt() {
        println("TransformAt")
        val t = transformationAt(Point.xyz(1.0, 2.0, 3.0),
                translation(Vector(-2.3, 5.4, 0.5))).invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(t).isEqualToPoint(Point.xyz(1.0, 3.0, -0.5))
        val r = transformationAt(Point.xyz(1.0, -1.0, 1.0),
                rotation(Vector(1.0, 1.0, 1.0), Math.PI / 3.0)).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val s = transformationAt(Point.xyz(1.0, 1.0, 1.0),
                scaling(2.3, 5.4, 0.5)).invoke(Point.xyz(3.0, -2.0, -1.0))
        pointAssertThat(s).isEqualToPoint(Point.xyz(5.6, -15.2, 0.0))
    }

    @Test
    fun testSimilarity() {
        println("similarity")
        val s = similarity(Tuple(Point.xyz(1.0, 0.0, 0.0), Point.xyz(0.0, 1.0, 0.0)), Tuple(Point.xyz(1.0, 1.0, -1.0), Point.xyz(-1.0, 1.0, 1.0)))
                .invoke(Point.xyz(0.0, 0.0, 1.0))
        pointAssertThat(s).isEqualToPoint(Point.xyz(1.0, -1.0, 1.0))
    }

    @Test
    fun testCalibrate() {
        println("calibrate")
        val c = calibrate(
                Tuple(Point.xyz(1.0, 0.0, 0.0), Point.xyz(0.0, 1.0, 0.0), Point.xyz(0.0, 0.0, 1.0), Point.xyz(-1.0, -1.0, -1.0)),
                Tuple(Point.xyz(1.0, -1.0, 1.0), Point.xyz(1.0, 1.0, -1.0), Point.xyz(-1.0, 1.0, 1.0), Point.xyz(1.0, 1.0, 1.0)))
        val c0 = c.invoke(Point.xyz(1.0, 0.0, 0.0))
        pointAssertThat(c0).isEqualToPoint(Point.xyz(1.0, -1.0, 1.0))
        val c1 = c.invoke(Point.xyz(0.0, 1.0, 0.0))
        pointAssertThat(c1).isEqualToPoint(Point.xyz(1.0, 1.0, -1.0))
        val c2 = c.invoke(Point.xyz(0.0, 0.0, 1.0))
        pointAssertThat(c2).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val c3 = c.invoke(Point.xyz(-1.0, -1.0, -1.0))
        pointAssertThat(c3).isEqualToPoint(Point.xyz(1.0, 1.0, 1.0))
        val r0 = c.invoke(Point.xyz(0.0, 0.0, 0.0))
        pointAssertThat(r0).isEqualToPoint(Point.xyz(0.5, 0.5, 0.5))
    }
    @Test
    fun testScale() {
        println("Scale")
        val s0 = identity.andScale(2.0, 3.0, 4.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        pointAssertThat(s0).isEqualToPoint(Point.xyz(4.0, -6.0, -4.0))

        val s1 = identity.andScale(2.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        pointAssertThat(s1).isEqualToPoint(Point.xyz(4.0, -4.0, -2.0))

        val s2 = identity.andScaleAt(Point.xyz(1.0, 1.0, 1.0), 2.0, 3.0, 4.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        pointAssertThat(s2).isEqualToPoint(Point.xyz(3.0, -8.0, -7.0))

        val s3 = identity.andScaleAt(Point.xyz(1.0, 1.0, 1.0), 3.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        pointAssertThat(s3).isEqualToPoint(Point.xyz(4.0, -8.0, -5.0))
    }

    @Test
    fun testRotate() {
        println("Rotate")
        val r0 = identity.andRotate(Vector(1.0, 1.0, 1.0), Math.PI * 2.0 / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r0).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r1 = identity.andRotate(Point.xyz(0.0, 0.0, -1.0), Point.xyz(1.0, 1.0, 0.0), Math.PI * 2.0 / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r1).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r2 = identity.andRotateAt(Point.xyz(1.0, 1.0, 1.0), Vector(0.0, 1.0, 0.0), Math.PI / 2).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r2).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r3 = identity.andRotate(Vector(0.0, 1.0, -1.0), Vector(-1.0, 1.0, 0.0), Math.PI / 3.0 * 2.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r3).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r4 = identity.andRotate(Vector(0.0, 1.0, -1.0), Vector(-1.0, 0.0, 1.0)).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r4).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r5 = identity.andRotateAt(Point.xyz(1.0, -1.0, 1.0), Vector(0.0, 1.0, -1.0), Vector(-1.0, 0.0, 1.0), Math.PI / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r5).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r6 = identity.andRotateAt(Point.xyz(1.0, -1.0, 1.0), Vector(0.0, 2.0, -2.0), Vector(-2.0, 2.0, 0.0)).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r6).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))

    }

    @Test
    fun testTranslate() {
        println("Translate")
        val t0 = identity.andTranslate(Vector(-2.3, 5.4, 0.5)).invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(t0).isEqualToPoint(Point.xyz(1.0, 3.0, -0.5))
        val t1 = identity.andTranslate(-2.3, 5.4, 0.5).invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(t1).isEqualToPoint(Point.xyz(1.0, 3.0, -0.5))
    }
}