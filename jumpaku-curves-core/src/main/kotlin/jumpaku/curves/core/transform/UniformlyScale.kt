package jumpaku.curves.core.transform


import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix


class UniformlyScale(val scale: Double = 1.0) : Transform {

    override val matrix: RealMatrix = MatrixUtils.createRealDiagonalMatrix(doubleArrayOf(scale, scale, scale, 1.0))

    override fun toString(): String = "UniformlyScale(scale=$scale)"


}

