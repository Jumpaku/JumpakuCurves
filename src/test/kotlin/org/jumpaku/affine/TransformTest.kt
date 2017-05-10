package org.jumpaku.affine

import org.junit.Test

/**
 * Created by jumpaku on 2017/05/10.
 */
class TransformTest {
    @Test
    fun testInvoke() {
        println("Invoke")
        val t = translation(Vector(2.3, -5.4, -0.5)).invoke(Crisp(3.3, -2.4, -1.0))
        pointAssertThat(t).isEqualToPoint(Crisp(5.6, -7.8, -1.5))
        val r = rotation(Vector(1.0, 1.0, 1.0), -Math.PI * 4.0 / 3.0).invoke(Crisp(1.0, 1.0, -1.0))
        pointAssertThat(r).isEqualToPoint(Crisp(-1.0, 1.0, 1.0))
        val s = scaling(0.5, 0.5, 2.0).invoke(Crisp(3.0, -2.0, -1.0))
        pointAssertThat(s).isEqualToPoint(Crisp(1.5, -1.0, -2.0))
    }

    @Test
    fun testInvert() {
        println("Invert")
        val ti = translation(Vector(-2.3, 5.4, 0.5)).invert().invoke(Crisp(3.3, -2.4, -1.0))
        pointAssertThat(ti).isEqualToPoint(Crisp(5.6, -7.8, -1.5))
        val ri = rotation(Vector(1.0, 1.0, 1.0), Math.PI * 4.0 / 3.0).invert().invoke(Crisp(1.0, 1.0, -1.0))
        pointAssertThat(ri).isEqualToPoint(Crisp(-1.0, 1.0, 1.0))
        val si = scaling(2.0, 2.0, 0.5).invert().invoke(Crisp(3.0, -2.0, -1.0))
        pointAssertThat(si).isEqualToPoint(Crisp(1.5, -1.0, -2.0))
    }

    @Test
    fun testConcatenate() {
        println("Concatenate")
        val c = ID
                .concatenate(translation(Vector(-2.0, 5.0, 1.0)))
                .concatenate(rotation(Vector(1.0, 1.0, 1.0), Math.PI*2.0/3.0))
                .invoke(Crisp(3.0, -2.0, -1.0))
        pointAssertThat(c).isEqualToPoint(Crisp(0.0, 1.0, 3.0))
    }

    @Test
    fun testTransformAt() {
        println("TransformAt")
        val t = transformationAt(Crisp(1.0, 2.0, 3.0),
                translation(Vector(-2.3, 5.4, 0.5))).invoke(Crisp(3.3, -2.4, -1.0))
        pointAssertThat(t).isEqualToPoint(Crisp(1.0, 3.0, -0.5))
        val r = transformationAt(Crisp(1.0, -1.0, 1.0),
                rotation(Vector(1.0, 1.0, 1.0), Math.PI / 3.0)).invoke(Crisp(1.0, 1.0, -1.0))
        pointAssertThat(r).isEqualToPoint(Crisp(-1.0, 1.0, 1.0))
        val s = transformationAt(Crisp(1.0, 1.0, 1.0),
                scaling(2.3, 5.4, 0.5)).invoke(Crisp(3.0, -2.0, -1.0))
        pointAssertThat(s).isEqualToPoint(Crisp(5.6, -15.2, 0.0))
    }

    @Test
    fun testSimilarity() {
        println("similarity")
        val s = similarity(Pair(Crisp(1.0, 0.0, 0.0), Crisp(0.0, 1.0, 0.0)), Pair(Crisp(1.0, 1.0, -1.0), Crisp(-1.0, 1.0, 1.0)))
                .invoke(Crisp(0.0, 0.0, 1.0))
        pointAssertThat(s).isEqualToPoint(Crisp(1.0, -1.0, 1.0))
    }

    @Test
    fun testCalibrate() {
        println("calibrate")
        val c = calibrate(
                Crisp(1.0, 0.0, 0.0), Crisp(0.0, 1.0, 0.0), Crisp(0.0, 0.0, 1.0), Crisp(-1.0, -1.0, -1.0),
                Crisp(1.0, -1.0, 1.0), Crisp(1.0, 1.0, -1.0), Crisp(-1.0, 1.0, 1.0), Crisp(1.0, 1.0, 1.0))
        val c0 = c.invoke(Crisp(1.0, 0.0, 0.0))
        pointAssertThat(c0).isEqualToPoint(Crisp(1.0, -1.0, 1.0))
        val c1 = c.invoke(Crisp(0.0, 1.0, 0.0))
        pointAssertThat(c1).isEqualToPoint(Crisp(1.0, 1.0, -1.0))
        val c2 = c.invoke(Crisp(0.0, 0.0, 1.0))
        pointAssertThat(c2).isEqualToPoint(Crisp(-1.0, 1.0, 1.0))
        val c3 = c.invoke(Crisp(-1.0, -1.0, -1.0))
        pointAssertThat(c3).isEqualToPoint(Crisp(1.0, 1.0, 1.0))
        val r0 = c.invoke(Crisp(0.0, 0.0, 0.0))
        pointAssertThat(r0).isEqualToPoint(Crisp(0.5, 0.5, 0.5))
    }
    @Test
    fun testScale() {
        println("Scale")
        val s0 = ID.scale(2.0, 3.0, 4.0).invoke(Crisp(2.0, -2.0, -1.0))
        pointAssertThat(s0).isEqualToPoint(Crisp(4.0, -6.0, -4.0))

        val s1 = ID.scale(2.0).invoke(Crisp(2.0, -2.0, -1.0))
        pointAssertThat(s1).isEqualToPoint(Crisp(4.0, -4.0, -2.0))

        val s2 = ID.scaleAt(Crisp(1.0, 1.0, 1.0), 2.0, 3.0, 4.0).invoke(Crisp(2.0, -2.0, -1.0))
        pointAssertThat(s2).isEqualToPoint(Crisp(3.0, -8.0, -7.0))

        val s3 = ID.scaleAt(Crisp(1.0, 1.0, 1.0), 3.0).invoke(Crisp(2.0, -2.0, -1.0))
        pointAssertThat(s3).isEqualToPoint(Crisp(4.0, -8.0, -5.0))
    }

    @Test
    fun testRotate() {
        println("Rotate")
        val r0 = ID.rotate(Vector(1.0, 1.0, 1.0), Math.PI * 2.0 / 3.0).invoke(Crisp(1.0, 1.0, -1.0))
        pointAssertThat(r0).isEqualToPoint(Crisp(-1.0, 1.0, 1.0))
        val r1 = ID.rotate(Crisp(0.0, 0.0, -1.0), Crisp(1.0, 1.0, 0.0), Math.PI * 2.0 / 3.0).invoke(Crisp(1.0, 1.0, -1.0))
        pointAssertThat(r1).isEqualToPoint(Crisp(-1.0, 1.0, 1.0))
        val r2 = ID.rotateAt(Crisp(1.0, 1.0, 1.0), Vector(0.0, 1.0, 0.0), Math.PI / 2).invoke(Crisp(1.0, 1.0, -1.0))
        pointAssertThat(r2).isEqualToPoint(Crisp(-1.0, 1.0, 1.0))
        val r3 = ID.rotate(Vector(0.0, 1.0, -1.0), Vector(-1.0, 1.0, 0.0), Math.PI / 3.0 * 2.0).invoke(Crisp(1.0, 1.0, -1.0))
        pointAssertThat(r3).isEqualToPoint(Crisp(-1.0, 1.0, 1.0))
        val r4 = ID.rotate(Vector(0.0, 1.0, -1.0), Vector(-1.0, 0.0, 1.0)).invoke(Crisp(1.0, 1.0, -1.0))
        pointAssertThat(r4).isEqualToPoint(Crisp(-1.0, 1.0, 1.0))
        val r5 = ID.rotateAt(Crisp(1.0, -1.0, 1.0), Vector(0.0, 1.0, -1.0), Vector(-1.0, 0.0, 1.0), Math.PI / 3.0).invoke(Crisp(1.0, 1.0, -1.0))
        pointAssertThat(r5).isEqualToPoint(Crisp(-1.0, 1.0, 1.0))
        val r6 = ID.rotateAt(Crisp(1.0, -1.0, 1.0), Vector(0.0, 2.0, -2.0), Vector(-2.0, 2.0, 0.0)).invoke(Crisp(1.0, 1.0, -1.0))
        pointAssertThat(r6).isEqualToPoint(Crisp(-1.0, 1.0, 1.0))

    }

    @Test
    fun testTranslate() {
        println("Translate")
        val t0 = ID.translate(Vector(-2.3, 5.4, 0.5)).invoke(Crisp(3.3, -2.4, -1.0))
        pointAssertThat(t0).isEqualToPoint(Crisp(1.0, 3.0, -0.5))
        val t1 = ID.translate(-2.3, 5.4, 0.5).invoke(Crisp(3.3, -2.4, -1.0))
        pointAssertThat(t1).isEqualToPoint(Crisp(1.0, 3.0, -0.5))
    }
}