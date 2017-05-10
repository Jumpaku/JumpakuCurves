package org.jumpaku.affine

/**
 * Created by jumpaku on 2017/05/09.
 */

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.FastMath;


fun translation(v: Vector): Transform{
    return Transform(MatrixUtils.createRealMatrix(arrayOf(
            doubleArrayOf(1.0, 0.0, 0.0, v.x),
            doubleArrayOf(1.0, 0.0, 0.0, v.y),
            doubleArrayOf(1.0, 0.0, 0.0, v.z),
            doubleArrayOf(1.0, 0.0, 0.0, 1.0)
    )))
}

fun rotation(axis: Vector, radian: Double): Transform{
    val normalized = axis.normalize()
    val x= normalized.x
    val y= normalized.y
    val z= normalized.z
    val cos = FastMath.cos(radian)
    val sin = FastMath.sin(radian)
    return Transform(MatrixUtils.createRealMatrix(arrayOf(
        doubleArrayOf(x*x*(1-cos)+cos,   x*y*(1-cos)-z*sin, z*x*(1-cos)+y*sin, 0.0),
        doubleArrayOf(x*y*(1-cos)+z*sin, y*y*(1-cos)+cos,   y*z*(1-cos)-x*sin, 0.0 ),
        doubleArrayOf(z*x*(1-cos)-y*sin, y*z*(1-cos)+x*sin, z*z*(1-cos)+cos,   0.0 ),
        doubleArrayOf(0.0,               0.0,               0.0,               1.0 )
    )))
}

fun scaling(x: Double, y: Double, z: Double): Transform {
    return Transform(MatrixUtils.createRealDiagonalMatrix(
            doubleArrayOf(x, y, z, 1.0)))
}

fun transformationAt(p: Crisp, a: Transform): Transform {
    return translation(p.toVector().negate()).concatenate(a).translate(p.toVector());
}

fun similarity(ab: Pair<Crisp, Crisp>, cd: Pair<Crisp, Crisp>): Transform{
    val a = ab.second.minus(ab.first)
    val b = cd.second.minus(cd.first)
    val ac = cd.first.minus(ab.first)
    return ID.rotateAt(ab.first, a, b).scaleAt(ab.first, b.length()/a.length()).translate(ac)
}


fun calibrate(before1: Crisp, before2: Crisp, before3: Crisp, before4: Crisp,
              after1: Crisp, after2: Crisp, after3: Crisp, after4: Crisp): Transform {
    val a = MatrixUtils.createRealMatrix(arrayOf(
            doubleArrayOf(before1.x, before1.y, before1.z, 1.0),
            doubleArrayOf(before2.x, before2.y, before2.z, 1.0),
            doubleArrayOf(before3.x, before3.y, before3.z, 1.0),
            doubleArrayOf(before4.x, before4.y, before4.z, 1.0)))
            .transpose()
    val b = MatrixUtils.createRealMatrix(arrayOf(
            doubleArrayOf(after1.x, after1.y, after1.z, 1.0),
            doubleArrayOf(after2.x, after2.y, after2.z, 1.0),
            doubleArrayOf(after3.x, after3.y, after3.z, 1.0),
            doubleArrayOf(after4.x, after4.y, after4.z, 1.0)))
            .transpose()

    return Transform(b.multiply(MatrixUtils.inverse(a)));
}

val ID = Transform(MatrixUtils.createRealIdentityMatrix(4))

class Transform internal constructor(private val matrix: RealMatrix) : Function1<Crisp, Crisp>{

    override fun invoke(p: Crisp): Crisp {
        val array = matrix.operate(doubleArrayOf(p.x, p.y, p.z, 1.0))
        return Crisp(array[0], array[1], array[2])
    }

    fun invert(): Transform = Transform(MatrixUtils.inverse(matrix))

    fun concatenate(a: Transform) = Transform(a.matrix.multiply(matrix))

    fun transformAt(p: Crisp, a: Transform): Transform = concatenate(transformationAt(p, a))

    fun scale(x: Double , y: Double, z: Double): Transform = concatenate(scaling(x, y, z))

    fun scale(scale: Double): Transform = scale(scale, scale, scale)

    fun scaleAt(center: Crisp, x: Double, y: Double, z: Double): Transform = transformAt(center, scaling(x, y, z))

    fun scaleAt(center: Crisp, scale: Double): Transform = scaleAt(center, scale, scale, scale)

    fun rotate(axis: Vector, radian: Double): Transform = concatenate(rotation(axis, radian))

    fun rotate(axisInitial: Crisp , axisTerminal: Crisp, radian: Double): Transform = rotate(axisTerminal.minus(axisInitial), radian)

    fun rotate(from: Vector, to: Vector, radian: Double): Transform = rotate(from.cross(to), radian)

    fun rotate(from: Vector, to: Vector): Transform = rotate(from, to, from.angle(to))

    fun rotateAt(center: Crisp, axis: Vector, radian: Double): Transform = transformAt(center, rotation(axis, radian))

    fun rotateAt(p: Crisp, from: Vector, to: Vector, radian: Double): Transform = transformAt(p, rotation(from.cross(to), radian))

    fun rotateAt(p: Crisp, from: Vector, to: Vector): Transform = rotateAt(p, from, to, from.angle(to))

    fun translate(v: Vector): Transform = concatenate(translation(v))

    fun translate(x: Double, y: Double, z: Double): Transform = translate(Vector(x, y, z))
}