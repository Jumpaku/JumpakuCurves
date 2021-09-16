package jumpaku.curves.core.transform

import jumpaku.curves.core.geom.Vector
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix


class Translate(val move: Vector = Vector.Zero) : AffineTransform {

    constructor(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) : this(Vector(x, y, z))

    override val matrix: RealMatrix = MatrixUtils.createRealMatrix(arrayOf(
            doubleArrayOf(1.0, 0.0, 0.0, move.x),
            doubleArrayOf(0.0, 1.0, 0.0, move.y),
            doubleArrayOf(0.0, 0.0, 1.0, move.z),
            doubleArrayOf(0.0, 0.0, 0.0, 1.0)
    ))

    override fun toString(): String = "Translate(move=$move)"
}

