package jumpaku.core.affine


import io.vavr.Tuple2
import io.vavr.Tuple4
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.util.FastMath


/**
 * Transforms a crisp point by affine transformation.
 */
class Affine internal constructor(private val matrix: RealMatrix) : Function1<Point, Point>{

    /**
     * @return transformed crisp point
     */
    override fun invoke(p: Point): Point {
        val array = matrix.operate(doubleArrayOf(p.x, p.y, p.z, 1.0))
        return Point.xyz(array[0], array[1], array[2])
    }

    fun invert(): Affine = Affine(MatrixUtils.inverse(matrix))

    fun andThen(a: Affine) = Affine(a.matrix.multiply(matrix))

    fun transformAt(p: Point, a: Affine): Affine = andThen(transformationAt(p, a))

    fun scale(x: Double , y: Double, z: Double): Affine = andThen(scaling(x, y, z))

    fun scale(scale: Double): Affine = scale(scale, scale, scale)

    fun scaleAt(center: Point, x: Double, y: Double, z: Double): Affine = transformAt(center, scaling(x, y, z))

    fun scaleAt(center: Point, scale: Double): Affine = scaleAt(center, scale, scale, scale)

    fun rotate(axis: Vector, radian: Double): Affine = andThen(rotation(axis, radian))

    fun rotate(axisInitial: Point, axisTerminal: Point, radian: Double): Affine = rotate(axisTerminal.minus(axisInitial), radian)

    fun rotate(from: Vector, to: Vector, radian: Double): Affine = rotate(from.cross(to), radian)

    fun rotate(from: Vector, to: Vector): Affine = rotate(from, to, from.angle(to))

    fun rotateAt(center: Point, axis: Vector, radian: Double): Affine = transformAt(center, rotation(axis, radian))

    fun rotateAt(p: Point, from: Vector, to: Vector, radian: Double): Affine = transformAt(p, rotation(from.cross(to), radian))

    fun rotateAt(p: Point, from: Vector, to: Vector): Affine = rotateAt(p, from, to, from.angle(to))

    fun translate(v: Vector): Affine = andThen(translation(v))

    fun translate(x: Double, y: Double, z: Double): Affine = translate(Vector(x, y, z))

    companion object {

        val ID = Affine(MatrixUtils.createRealIdentityMatrix(4))

        fun translation(v: Vector): Affine {
            return Affine(MatrixUtils.createRealMatrix(arrayOf(
                    doubleArrayOf(1.0, 0.0, 0.0, v.x),
                    doubleArrayOf(0.0, 1.0, 0.0, v.y),
                    doubleArrayOf(0.0, 0.0, 1.0, v.z),
                    doubleArrayOf(0.0, 0.0, 0.0, 1.0)
            )))
        }

        fun rotation(axis: Vector, radian: Double): Affine {
            val normalized = axis.normalize()
            val x = normalized.x
            val y = normalized.y
            val z = normalized.z
            val cos = FastMath.cos(radian)
            val sin = FastMath.sin(radian)
            return Affine(MatrixUtils.createRealMatrix(arrayOf(
                    doubleArrayOf(x * x * (1 - cos) + cos, x * y * (1 - cos) - z * sin, z * x * (1 - cos) + y * sin, 0.0),
                    doubleArrayOf(x * y * (1 - cos) + z * sin, y * y * (1 - cos) + cos, y * z * (1 - cos) - x * sin, 0.0),
                    doubleArrayOf(z * x * (1 - cos) - y * sin, y * z * (1 - cos) + x * sin, z * z * (1 - cos) + cos, 0.0),
                    doubleArrayOf(0.0, 0.0, 0.0, 1.0)
            )))
        }

        fun scaling(x: Double, y: Double, z: Double): Affine {
            return Affine(MatrixUtils.createRealDiagonalMatrix(
                    doubleArrayOf(x, y, z, 1.0)))
        }

        fun transformationAt(p: Point, a: Affine): Affine {
            return translation(p.toVector().unaryMinus()).andThen(a).translate(p.toVector())
        }

        fun similarity(ab: Tuple2<Point, Point>, cd: Tuple2<Point, Point>): Affine {
            val a = ab._2().minus(ab._1())
            val b = cd._2().minus(cd._1())
            val ac = cd._1().minus(ab._1())
            return ID.rotateAt(ab._1(), a, b).scaleAt(ab._1(), b.length()/a.length()).translate(ac)
        }


        fun calibrate(before: Tuple4<Point, Point, Point, Point>,
                      after: Tuple4<Point, Point, Point, Point>): Affine {
            val a = MatrixUtils.createRealMatrix(arrayOf(
                    doubleArrayOf(before._1().x, before._1().y, before._1().z, 1.0),
                    doubleArrayOf(before._2().x, before._2().y, before._2().z, 1.0),
                    doubleArrayOf(before._3().x, before._3().y, before._3().z, 1.0),
                    doubleArrayOf(before._4().x, before._4().y, before._4().z, 1.0)))
                    .transpose()
            val b = MatrixUtils.createRealMatrix(arrayOf(
                    doubleArrayOf(after._1().x, after._1().y, after._1().z, 1.0),
                    doubleArrayOf(after._2().x, after._2().y, after._2().z, 1.0),
                    doubleArrayOf(after._3().x, after._3().y, after._3().z, 1.0),
                    doubleArrayOf(after._4().x, after._4().y, after._4().z, 1.0)))
                    .transpose()

            return Affine(b.multiply(MatrixUtils.inverse(a)))
        }

    }
}