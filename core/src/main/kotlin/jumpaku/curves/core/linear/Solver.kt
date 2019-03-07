package jumpaku.curves.core.linear

import jumpaku.curves.core.util.Result
import jumpaku.curves.core.util.result
import jumpaku.curves.core.util.tryDiv
import org.apache.commons.math3.linear.*

interface Solver {

    fun solve(b: Matrix): Matrix

    fun inverse(): Matrix

    companion object {

        fun of(matrix: Matrix): Result<Solver> = result {
            when(matrix) {
                is Matrix.Identity -> IdentitySolver(matrix)
                is Matrix.Diagonal -> DiagonalSolver(matrix)
                is Matrix.Sparse -> SparseQRSolver(matrix)
                is Matrix.Array2D -> Array2DQRSolver(matrix)
            }
        }
    }
}

private fun Matrix.toRealMatrix(): RealMatrix = when(this) {
    is Matrix.Identity -> DiagonalMatrix(DoubleArray(rowSize) { 1.0 })
    is Matrix.Diagonal -> DiagonalMatrix(data.toDoubleArray())
    is Matrix.Sparse -> OpenMapRealMatrix(rowSize, columnSize).apply {
        this@toRealMatrix.data.forEach { (i, j), value -> setEntry(i, j, value) }
    }
    is Matrix.Array2D -> MatrixUtils.createRealMatrix(toDoubleArrays())
}

private fun RealMatrix.toSparse(): Matrix.Sparse = Matrix.Sparse(rowDimension, columnDimension, {
    val m = mutableMapOf<Matrix.Sparse.Key, Double>()
    for (i in 0 until rowDimension) {
        for (j in 0 until columnDimension) {
            getEntry(i, j).let { if (1.0.tryDiv(it).isSuccess) m[Matrix.Sparse.Key(i, j)] = it }
        }
    }
    m
}())

private fun RealMatrix.toArray2D(): Matrix.Array2D =
        Matrix.Array2D(rowDimension, columnDimension) { i, j -> getEntry(i, j) }

private class IdentitySolver(val identity: Matrix.Identity): Solver {

    override fun solve(b: Matrix): Matrix = b

    override fun inverse(): Matrix = identity
}

private class DiagonalSolver(diagonal: Matrix.Diagonal): Solver {

    val inverse: Matrix.Diagonal = Matrix.Diagonal(diagonal.data.map { 1.0.tryDiv(it).orThrow() })

    override fun solve(b: Matrix): Matrix = inverse*b

    override fun inverse(): Matrix = inverse
}

private class SparseQRSolver(sparse: Matrix.Sparse): Solver {

    val solver: DecompositionSolver = QRDecomposition(sparse.toRealMatrix()).solver

    init {
        check(solver.isNonSingular) { "singular matrix" }
    }

    override fun solve(b: Matrix): Matrix = solver.solve(b.toRealMatrix()).toSparse()

    override fun inverse(): Matrix = solver.inverse.toSparse()
}

private class Array2DQRSolver(array2D: Matrix.Array2D): Solver {

    val solver: DecompositionSolver = QRDecomposition(array2D.toRealMatrix()).solver

    init {
        check(solver.isNonSingular) { "singular matrix" }
    }

    override fun solve(b: Matrix): Matrix = solver.solve(b.toRealMatrix()).toArray2D()

    override fun inverse(): Matrix = solver.inverse.toArray2D()
}

