package jumpaku.curves.core.linear

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.OpenMapRealMatrix
import org.apache.commons.math3.linear.RealMatrix


internal fun convertToRealMatrix(matrix: Matrix): RealMatrix =
        if (matrix is MatrixImpl) matrix.realMatrix
        else Array2DRowRealMatrix(matrix.toDoubleArrays())

internal class MatrixImpl(realMatrix: RealMatrix): Matrix {

    val realMatrix: RealMatrix = realMatrix.copy()

    override val rowSize: Int get() = realMatrix.rowDimension

    override val columnSize: Int get() = realMatrix.columnDimension

    override fun times(other: Matrix): Matrix = MatrixImpl(realMatrix.multiply(convertToRealMatrix(other)))

    override fun transpose(): Matrix = MatrixImpl(realMatrix.transpose())

    override fun get(i: Int, j: Int): Double = realMatrix.getEntry(i, j)
}

interface Matrix {

    data class Key(val row: Int, val column: Int)

    val rowSize: Int

    val columnSize: Int

    operator fun times(other: Matrix): Matrix

    fun transpose(): Matrix

    operator fun get(i: Int, j: Int): Double

    operator fun get(key: Key): Double = get(key.row, key.column)

    fun toDoubleArrays(): Array<DoubleArray> = Array(rowSize) { i -> DoubleArray(columnSize) { j -> get(i, j) } }

    companion object {

        fun sparse(rowSize: Int, columnSize: Int, data: Map<Key, Double>): Matrix =
                MatrixImpl(OpenMapRealMatrix(rowSize, columnSize).apply {
                    data.forEach { (i, j), value -> setEntry(i, j, value) }
                })

        fun diagonal(data: List<Double>): Matrix =
                sparse(data.size, data.size, data.mapIndexed { i, v -> Key(i, i) to v }.toMap())

        fun identity(size: Int): Matrix = diagonal(List(size) { 1.0 })

        fun of(data: List<List<Double>>): Matrix = of(data.map { it.toDoubleArray() }.toTypedArray())

        fun of(data: Array<DoubleArray>): Matrix = MatrixImpl(realMatrix = Array2DRowRealMatrix(data))
    }
}
