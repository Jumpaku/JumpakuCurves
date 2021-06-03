package jumpaku.curves.core.transform

import jumpaku.commons.control.Option
import jumpaku.commons.control.optionWhen
import jumpaku.curves.core.geom.Point
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
    }
}


