package jumpaku.curves.core.linear

import jumpaku.curves.core.util.Result
import jumpaku.curves.core.util.result
import org.apache.commons.math3.linear.QRDecomposition

interface Solver {

    fun solve(b: Matrix): Matrix

    fun inverse(): Matrix

    companion object {

        fun byQRDecomposition(matrix: Matrix): Result<Solver> = result {

            val solver = QRDecomposition((matrix as MatrixImpl).realMatrix).solver
            check(solver.isNonSingular) { "singular matrix" }
            object : Solver {

                override fun solve(b: Matrix): Matrix = MatrixImpl(solver.solve(convertToRealMatrix(b)))

                override fun inverse(): Matrix = MatrixImpl(solver.inverse)
            }
        }
    }
}


