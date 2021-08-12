package jumpaku.curves.core.transform

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.geom.times
import org.apache.commons.math3.linear.*

class Calibrate(
    val pair0: Pair<Point, Point>,
    val pair1: Pair<Point, Point>,
    val pair2: Pair<Point, Point>,
    val pair3: Pair<Point, Point>
) : Transform {

    constructor(pair0: Pair<Point, Point>, pair1: Pair<Point, Point>, pair2: Pair<Point, Point>)
            : this(makePairs(pair0, pair1, pair2))

    constructor(pair0: Pair<Point, Point>, pair1: Pair<Point, Point>) : this(makePairs(pair0, pair1))

    constructor(pair0: Pair<Point, Point>) : this(makePairs(pair0))

    constructor() : this(makePairs(Point.origin to Point.origin))

    private constructor(pairs4: List<Pair<Point, Point>>) : this(pairs4[0], pairs4[1], pairs4[2], pairs4[3])

    override val matrix: RealMatrix

    init {
        val from = arrayOf(pair0, pair1, pair2, pair3)
            .map { (f, _) -> (f.toDoubleArray() + 1.0) }
            .run { (MatrixUtils.createRealMatrix(toTypedArray())) }
        val to = arrayOf(pair0, pair1, pair2, pair3)
            .map { (_, t) -> (t.toDoubleArray() + 1.0) }
            .run { (MatrixUtils.createRealMatrix(toTypedArray())) }
        matrix = QRDecomposition(from).solver.solve(to).transpose()
    }

    companion object {

        private fun makePairs(pair0: Pair<Point, Point>, pair1: Pair<Point, Point>, pair2: Pair<Point, Point>)
                : List<Pair<Point, Point>> {
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
            return listOf(Vector.Zero, Vector.I, Vector.J, Vector.K).map {
                val (s, t) = solver.solve(
                    ArrayRealVector(
                        doubleArrayOf(
                            it.dot(u),
                            it.dot(v)
                        )
                    )
                ).toArray()
                (fromO + it) to Point((1 - s - t) * toO.toVector() + s * toA.toVector() + t * toB.toVector())
            }
        }

        private fun makePairs(pair0: Pair<Point, Point>, pair1: Pair<Point, Point>): List<Pair<Point, Point>> {
            val (fromO, toO) = pair0
            val (fromP, toP) = pair1
            val v = fromP - fromO
            return listOf(Vector.Zero, Vector.I, Vector.J, Vector.K)
                .map { fromO + it to toO.lerp(v.dot(it) / v.square(), toP) }
        }

        private fun makePairs(pair0: Pair<Point, Point>): List<Pair<Point, Point>> {
            val (from, to) = pair0
            return listOf(Vector.Zero, Vector.I, Vector.J, Vector.K)
                .map { from + it to to }
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
            val f2 = f1.cross(f0)

            return Calibrate(listOf(Vector.Zero to Vector.Zero, e0 to f0, e1 to f1, e2 to f2).map { (a, b) ->
                fromO + a to toO + b
            })
        }
    }
}