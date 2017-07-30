package org.jumpaku.core.affine

import io.vavr.API.*
import org.junit.Test

/**
 * Created by jumpaku on 2017/05/10.
 */
class AffineTest {

    @Test
    fun testInvoke() {
        println("Invoke")
        val t = Affine.translation(Vector(2.3, -5.4, -0.5)).invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(t).isEqualToPoint(Point.xyz(5.6, -7.8, -1.5))
        val r = Affine.rotation(Vector(1.0, 1.0, 1.0), -Math.PI * 4.0 / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val s = Affine.scaling(0.5, 0.5, 2.0).invoke(Point.xyz(3.0, -2.0, -1.0))
        pointAssertThat(s).isEqualToPoint(Point.xyz(1.5, -1.0, -2.0))
    }

    @Test
    fun testInvert() {
        println("Invert")
        val ti = Affine.translation(Vector(-2.3, 5.4, 0.5)).invert().invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(ti).isEqualToPoint(Point.xyz(5.6, -7.8, -1.5))
        val ri = Affine.rotation(Vector(1.0, 1.0, 1.0), Math.PI * 4.0 / 3.0).invert().invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(ri).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val si = Affine.scaling(2.0, 2.0, 0.5).invert().invoke(Point.xyz(3.0, -2.0, -1.0))
        pointAssertThat(si).isEqualToPoint(Point.xyz(1.5, -1.0, -2.0))
    }

    @Test
    fun testConcatenate() {
        println("Concatenate")
        val c = Affine.ID
                .andThen(Affine.translation(Vector(-2.0, 5.0, 1.0)))
                .andThen(Affine.rotation(Vector(1.0, 1.0, 1.0), Math.PI*2.0/3.0))
                .invoke(Point.xyz(3.0, -2.0, -1.0))
        pointAssertThat(c).isEqualToPoint(Point.xyz(0.0, 1.0, 3.0))
    }

    @Test
    fun testTransformAt() {
        println("TransformAt")
        val t = Affine.transformationAt(Point.xyz(1.0, 2.0, 3.0),
                Affine.translation(Vector(-2.3, 5.4, 0.5))).invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(t).isEqualToPoint(Point.xyz(1.0, 3.0, -0.5))
        val r = Affine.transformationAt(Point.xyz(1.0, -1.0, 1.0),
                Affine.rotation(Vector(1.0, 1.0, 1.0), Math.PI / 3.0)).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val s = Affine.transformationAt(Point.xyz(1.0, 1.0, 1.0),
                Affine.scaling(2.3, 5.4, 0.5)).invoke(Point.xyz(3.0, -2.0, -1.0))
        pointAssertThat(s).isEqualToPoint(Point.xyz(5.6, -15.2, 0.0))
    }

    @Test
    fun testSimilarity() {
        println("similarity")
        val s = Affine.similarity(Tuple(Point.xyz(1.0, 0.0, 0.0), Point.xyz(0.0, 1.0, 0.0)), Tuple(Point.xyz(1.0, 1.0, -1.0), Point.xyz(-1.0, 1.0, 1.0)))
                .invoke(Point.xyz(0.0, 0.0, 1.0))
        pointAssertThat(s).isEqualToPoint(Point.xyz(1.0, -1.0, 1.0))
    }

    @Test
    fun testCalibrate() {
        println("calibrate")
        val c = Affine.calibrate(
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
        val s0 = Affine.ID.scale(2.0, 3.0, 4.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        pointAssertThat(s0).isEqualToPoint(Point.xyz(4.0, -6.0, -4.0))

        val s1 = Affine.ID.scale(2.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        pointAssertThat(s1).isEqualToPoint(Point.xyz(4.0, -4.0, -2.0))

        val s2 = Affine.ID.scaleAt(Point.xyz(1.0, 1.0, 1.0), 2.0, 3.0, 4.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        pointAssertThat(s2).isEqualToPoint(Point.xyz(3.0, -8.0, -7.0))

        val s3 = Affine.ID.scaleAt(Point.xyz(1.0, 1.0, 1.0), 3.0).invoke(Point.xyz(2.0, -2.0, -1.0))
        pointAssertThat(s3).isEqualToPoint(Point.xyz(4.0, -8.0, -5.0))
    }

    @Test
    fun testRotate() {
        println("Rotate")
        val r0 = Affine.ID.rotate(Vector(1.0, 1.0, 1.0), Math.PI * 2.0 / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r0).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r1 = Affine.ID.rotate(Point.xyz(0.0, 0.0, -1.0), Point.xyz(1.0, 1.0, 0.0), Math.PI * 2.0 / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r1).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r2 = Affine.ID.rotateAt(Point.xyz(1.0, 1.0, 1.0), Vector(0.0, 1.0, 0.0), Math.PI / 2).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r2).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r3 = Affine.ID.rotate(Vector(0.0, 1.0, -1.0), Vector(-1.0, 1.0, 0.0), Math.PI / 3.0 * 2.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r3).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r4 = Affine.ID.rotate(Vector(0.0, 1.0, -1.0), Vector(-1.0, 0.0, 1.0)).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r4).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r5 = Affine.ID.rotateAt(Point.xyz(1.0, -1.0, 1.0), Vector(0.0, 1.0, -1.0), Vector(-1.0, 0.0, 1.0), Math.PI / 3.0).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r5).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))
        val r6 = Affine.ID.rotateAt(Point.xyz(1.0, -1.0, 1.0), Vector(0.0, 2.0, -2.0), Vector(-2.0, 2.0, 0.0)).invoke(Point.xyz(1.0, 1.0, -1.0))
        pointAssertThat(r6).isEqualToPoint(Point.xyz(-1.0, 1.0, 1.0))

    }

    @Test
    fun testTranslate() {
        println("Translate")
        val t0 = Affine.ID.translate(Vector(-2.3, 5.4, 0.5)).invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(t0).isEqualToPoint(Point.xyz(1.0, 3.0, -0.5))
        val t1 = Affine.ID.translate(-2.3, 5.4, 0.5).invoke(Point.xyz(3.3, -2.4, -1.0))
        pointAssertThat(t1).isEqualToPoint(Point.xyz(1.0, 3.0, -0.5))
    }
}