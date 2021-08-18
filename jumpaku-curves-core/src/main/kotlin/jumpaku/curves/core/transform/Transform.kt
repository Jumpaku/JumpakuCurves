package jumpaku.curves.core.transform

import jumpaku.commons.control.Option
import jumpaku.commons.control.optionWhen
import jumpaku.commons.control.result
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import org.apache.commons.math3.linear.EigenDecomposition
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.QRDecomposition
import org.apache.commons.math3.linear.RealMatrix

interface Transform : (Point) -> Point{

    val matrix: RealMatrix

    override operator fun invoke(p: Point): Point = matrix.operate(doubleArrayOf(p.x, p.y, p.z, 1.0))
            .let { Point.xyz(it[0], it[1], it[2]) }

    fun andThen(a: Transform): Transform = ofMatrix(a.matrix.multiply(this@Transform.matrix))

    fun at(origin: Point): Transform = Translate(-origin.toVector()).andThen(this).andThen(Translate(origin.toVector()))

    fun invert(): Option<Transform> = QRDecomposition(matrix).solver.run { optionWhen(isNonSingular) { ofMatrix(inverse) } }

    companion object {

        fun ofMatrix(m: RealMatrix): Transform = object : Transform {
            override val matrix: RealMatrix = m
        }

        val Identity = ofMatrix(MatrixUtils.createRealIdentityMatrix(4))

        fun calibrateByFitting(pairs: List<Pair<Point, Point>>): Transform {
            require(pairs.isNotEmpty()) { "pairs(${pairs.size}) must be not empty" }
            return result { fit(pairs, 3) }
                .tryRecover { fit(pairs, 2) }
                .tryRecover { fit(pairs, 1) }
                .orRecover { fit(pairs, 0) }
        }

        /**
         * Computes a coordinate system, which consists of an origin and three axes, based on eigen vectors obtained by principal component analysis.
         * The origin is the center of gravity of [points].
         * The first axis is a vector parallel to the line to which [points] fit.
         * The third axis is a normal vector of the plane to which [points] fit (http://sysplan.nams.kyushu-u.ac.jp/gen/edu/Algorithms/PlaneFitting/index.html)
         * @return coordinate system that consists of an origin and three axes.
         */
        fun pcaCoordinateSystem(points: List<Point>): Pair<Point, Triple<Vector, Vector, Vector>> {
            val n = points.size
            val origin = points[0].lerp(points.drop(1).map { (1.0 / n) to it })
            val matX = points.map { x -> (x - origin).toDoubleArray() }.toTypedArray().let(MatrixUtils::createRealMatrix)
            val solver = EigenDecomposition(matX.transpose().multiply(matX))
            val (axis0, axis1, axis2) = (0..2).map {
                val axes = solver.getEigenvector(it)
                Vector(axes.getEntry(0), axes.getEntry(1), axes.getEntry(2))
            }
            return origin to Triple(axis0, axis1, axis2)
        }

        private fun fit(pairs: List<Pair<Point, Point>>, dimension: Int): Transform {
            require(dimension in 0..3) { "dimension($dimension) must be in 0..3" }

            val (ps, qs) = pairs.let {
                val (_, vB) = pcaCoordinateSystem(it.map { it.first })
                when (dimension) {
                    3 -> it
                    2 -> it.flatMap { (p, q) ->
                        listOf(vB.third, -vB.third).map { v -> p + v to q }
                    }
                    1 -> it.flatMap { (p, q) ->
                        listOf(vB.third, -vB.third, vB.second, -vB.second).map { v -> p + v to q }
                    }
                    0 -> it.flatMap { (p, q) ->
                        listOf(vB.third, -vB.third, vB.second, -vB.second, vB.first, -vB.first).map { v -> p + v to q }
                    }
                    else -> error("")
                }.unzip()
            }
            val arrX = ps.map { (x, y, z) -> doubleArrayOf(x, y, z, 1.0) }
            val arrY = qs.map { (x, y, z) -> doubleArrayOf(x, y, z, 1.0) }

            val d = 3
            val matX = Array(arrX.size * d) { i ->
                val xc = arrX[i / d]
                when {
                    i % d == 0 -> xc + DoubleArray(d + 1) + DoubleArray(d + 1)
                    i % d == 1 -> DoubleArray(d + 1) + xc + DoubleArray(d + 1)
                    i % d == 2 -> DoubleArray(d + 1) + DoubleArray(d + 1) + xc
                    else -> error("Invalid dimension($dimension)")
                }
            }.let(MatrixUtils::createRealMatrix)
            val matXT = matX.transpose()
            val vecY = DoubleArray(arrY.size * d) { i -> arrY[i / d][i % d] }
                .let(MatrixUtils::createRealVector)
            val vecF = QRDecomposition(matXT.multiply(matX)).solver.solve(matXT.operate(vecY)).toArray()
            val matF = arrayOf(
                vecF.copyOfRange(0, 4),
                vecF.copyOfRange(4, 8),
                vecF.copyOfRange(8, 12),
                doubleArrayOf(0.0, 0.0, 0.0, 1.0),
            ).let(MatrixUtils::createRealMatrix)
            return ofMatrix(matF)
        }

    }
}


