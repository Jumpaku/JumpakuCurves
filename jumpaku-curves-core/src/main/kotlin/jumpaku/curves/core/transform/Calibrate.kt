package jumpaku.curves.core.transform

import jumpaku.commons.control.result
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import org.apache.commons.math3.linear.*

class Calibrate private constructor(override val matrix: RealMatrix) : AffineTransform {

    constructor(
        pair0: Pair<Point, Point>,
        pair1: Pair<Point, Point>,
        pair2: Pair<Point, Point>,
        pair3: Pair<Point, Point>
    ) : this(makeMatrix(pair0, pair1, pair2, pair3))

    constructor(pair0: Pair<Point, Point>, pair1: Pair<Point, Point>, pair2: Pair<Point, Point>)
            : this(makeMatrix(pair0, pair1, pair2))

    constructor(pair0: Pair<Point, Point>, pair1: Pair<Point, Point>) : this(makeMatrix(pair0, pair1))

    constructor(pair0: Pair<Point, Point>) : this(makeMatrix(pair0))

    companion object {

        fun makeMatrix(
            pair0: Pair<Point, Point>,
            pair1: Pair<Point, Point>,
            pair2: Pair<Point, Point>,
            pair3: Pair<Point, Point>
        ): RealMatrix {
            return result {
                val from = arrayOf(pair0, pair1, pair2, pair3)
                    .map { (f, _) -> (f.toDoubleArray() + 1.0) }
                    .run { (MatrixUtils.createRealMatrix(toTypedArray())) }
                val to = arrayOf(pair0, pair1, pair2, pair3)
                    .map { (_, t) -> (t.toDoubleArray() + 1.0) }
                    .run { (MatrixUtils.createRealMatrix(toTypedArray())) }
                QRDecomposition(from).solver.solve(to).transpose()
            }.orRecover {
                AffineTransform.calibrateByFitting(listOf(pair0, pair1, pair2, pair3)).matrix
            }

        }

        fun makeMatrix(pair0: Pair<Point, Point>, pair1: Pair<Point, Point>, pair2: Pair<Point, Point>): RealMatrix {
            val (fromO, toO) = pair0
            val (fromA, toA) = pair1
            val (fromB, toB) = pair2
            val u = fromA - fromO
            val v = fromB - fromO
            val m = Array2DRowRealMatrix(
                arrayOf(
                    doubleArrayOf(u.square(), u.dot(v)),
                    doubleArrayOf(u.dot(v), v.square())
                )
            )
            val solver = QRDecomposition(m).solver
            val (p0, p1, p2, p3) = listOf(Vector.Zero, Vector.I, Vector.J, Vector.K).map {
                val (s, t) = solver.solve(
                    ArrayRealVector(
                        doubleArrayOf(
                            it.dot(u),
                            it.dot(v)
                        )
                    )
                ).toArray()
                (fromO + it) to toO.lerp(s to toA, t to toB)
            }
            return makeMatrix(p0, p1, p2, p3)
        }


        fun makeMatrix(pair0: Pair<Point, Point>, pair1: Pair<Point, Point>): RealMatrix {
            val (fromO, toO) = pair0
            val (fromP, toP) = pair1
            val v = fromP - fromO
            val (p0, p1, p2, p3) = listOf(Vector.Zero, Vector.I, Vector.J, Vector.K)
                .map { fromO + it to toO.lerp(v.dot(it) / v.square(), toP) }
            return makeMatrix(p0, p1, p2, p3)
        }

        fun makeMatrix(pair0: Pair<Point, Point>): RealMatrix {
            val (from, to) = pair0
            val (p0, p1, p2, p3) = listOf(Vector.Zero, Vector.I, Vector.J, Vector.K)
                .map { from + it to to }
            return makeMatrix(p0, p1, p2, p3)
        }

        fun similarityWithNormal(
            pair0: Pair<Point, Point>, pair1: Pair<Point, Point>, normal: Pair<Vector, Vector>
        ): Calibrate {
            val (fromO, toO) = pair0
            val (fromP, toP) = pair1
            val (fromN, toN) = normal.let { (f, t) -> f.normalize().orThrow() to t.normalize().orThrow() }

            val e0 = fromP - fromO
            val e1 = e0.cross(fromN)
            val e2 = e1.cross(e0)

            val f0 = toP - toO
            val f1 = f0.cross(toN)
            //val f2 = f1.cross(f0)
            val (p0, p1, p2, p3) = listOf(Vector.Zero to Vector.Zero, e0 to f0, e1 to f1, e2 to Vector.Zero)
                .map { (a, b) -> fromO + a to toO + b }
            return Calibrate(p0, p1, p2, p3)
        }
    }
}