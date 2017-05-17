package org.jumpaku.affine

import io.vavr.API.*
import org.junit.Test

/**
 * Created by jumpaku on 2017/05/10.
 */
class TransformTest {

    @Test
    fun testInvoke() {
        println("Invoke")
        val t = Transform.translation(Vector(2.3, -5.4, -0.5)).invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(t).isEqualToPoint(Point.xyz(5.6, -7.8, -1.5))
        val r = Transform.rotation(Vector(1.0, 1.0, 1.0), -Math.PI * 4.0 / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val s = Transform.scaling(0.5, 0.5, 2.0).invoke(Point.xyz(3.0, -2.0, -1.0))
        pointAssertThat(s).isEqualToPoint(Point.xyz(1.5, -1.0, -2.0))
    }

    @Test
    fun testInvert() {
        println("Invert")
        val ti = Transform.translation(Vector(-2.3, 5.4, 0.5)).invert().invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(ti).isEqualToPoint(Point.xyz(5.6, -7.8, -1.5))
        val ri = Transform.rotation(Vector(1.0, 1.0, 1.0), Math.PI * 4.0 / 3.0).invert().invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(ri).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val si = Transform.scaling(2.0, 2.0, 0.5).invert().invoke(Point.xyz(3.0, -2.0, -1.0))
        pointAssertThat(si).isEqualToPoint(Point.xyz(1.5, -1.0, -2.0))
    }

    @Test
    fun testConcatenate() {
        println("Concatenate")
        val c = Transform.ID
                .andThen(Transform.translation(Vector(-2.0, 5.0, 1.0)))
                .andThen(Transform.rotation(Vector(1.0, 1.0, 1.0), Math.PI*2.0/3.0))
                .invoke(Point.xyz(3.0, -2.0, -1.0))
        pointAssertThat(c).isEqualToPoint(Point.xyz(0.0, 1.0, 3.0))
    }

    @Test
    fun testTransformAt() {
        println("TransformAt")
        val t = Transform.transformationAt(Point.xyz(1.0, 2.0, 3.0),
                Transform.translation(Vector(-2.3, 5.4, 0.5))).invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(t).isEqualToPoint(Point.xyz(1.0, 3.0, -0.5))
        val r = Transform.transformationAt(Point.xyz(1.0, -1.0, 1.0),
                Transform.rotation(Vector(1.0, 1.0, 1.0), Math.PI / 3.0)).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val s = Transform.transformationAt(Point.xyz(1.0, 1.0, 1.0),
                Transform.scaling(2.3, 5.4, 0.5)).invoke(Point.xyz(3.0, -2.0, -1.0))
        pointAssertThat(s).isEqualToPoint(Point.xyz(5.6, -15.2, 0.0))
    }

    @Test
    fun testSimilarity() {
        println("similarity")
        val s = Transform.similarity(Tuple(Point.xyz(1.0, 0.0, 0.0), Point.xyz(0.0, 1.0, 0.0)), Tuple(Point.xyz(1.0, 1.0, -1.0), Point.xyz(-1.0, 1.0, 1.0)))
                .invoke(Point.xyz(0.0, 0.0, 1.0))
        pointAssertThat(s).isEqualToPoint(Point.xyz(1.0, -1.0, 1.0))
    }

    @Test
    fun testCalibrate() {
        println("calibrate")
        val c = Transform.calibrate(
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
        val s0 = Transform.ID.scale(2.0, 3.0, 4.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        pointAssertThat(s0).isEqualToPoint(Point.xyz(4.0, -6.0, -4.0))

        val s1 = Transform.ID.scale(2.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        pointAssertThat(s1).isEqualToPoint(Point.xyz(4.0, -4.0, -2.0))

        val s2 = Transform.ID.scaleAt(Point.xyz(1.0, 1.0, 1.0), 2.0, 3.0, 4.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        pointAssertThat(s2).isEqualToPoint(Point.xyz(3.0, -8.0, -7.0))

        val s3 = Transform.ID.scaleAt(Point.xyz(1.0, 1.0, 1.0), 3.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        pointAssertThat(s3).isEqualToPoint(Point.xyz(4.0, -8.0, -5.0))
    }

    @Test
    fun testRotate() {
        println("Rotate")
        val r0 = Transform.ID.rotate(Vector(1.0, 1.0, 1.0), Math.PI * 2.0 / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r0).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r1 = Transform.ID.rotate(Point.xyz(0.0, 0.0, -1.0), Point.xyz(1.0, 1.0, 0.0), Math.PI * 2.0 / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r1).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r2 = Transform.ID.rotateAt(Point.xyz(1.0, 1.0, 1.0), Vector(0.0, 1.0, 0.0), Math.PI / 2).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r2).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r3 = Transform.ID.rotate(Vector(0.0, 1.0, -1.0), Vector(-1.0, 1.0, 0.0), Math.PI / 3.0 * 2.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r3).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r4 = Transform.ID.rotate(Vector(0.0, 1.0, -1.0), Vector(-1.0, 0.0, 1.0)).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r4).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r5 = Transform.ID.rotateAt(Point.xyz(1.0, -1.0, 1.0), Vector(0.0, 1.0, -1.0), Vector(-1.0, 0.0, 1.0), Math.PI / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r5).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r6 = Transform.ID.rotateAt(Point.xyz(1.0, -1.0, 1.0), Vector(0.0, 2.0, -2.0), Vector(-2.0, 2.0, 0.0)).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r6).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))

    }

    @Test
    fun testTranslate() {
        println("Translate")
        val t0 = Transform.ID.translate(Vector(-2.3, 5.4, 0.5)).invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(t0).isEqualToPoint(Point.xyz(1.0, 3.0, -0.5))
        val t1 = Transform.ID.translate(-2.3, 5.4, 0.5).invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(t1).isEqualToPoint(Point.xyz(1.0, 3.0, -0.5))
    }
}