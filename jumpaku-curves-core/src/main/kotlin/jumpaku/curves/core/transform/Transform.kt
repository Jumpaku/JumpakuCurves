package jumpaku.curves.core.transform

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.jsonArray
import com.google.gson.JsonElement
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.util.Option
import jumpaku.curves.core.util.optionWhen
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.QRDecomposition
import org.apache.commons.math3.linear.RealMatrix

interface Transform {

    val matrix: RealMatrix

    operator fun invoke(p: Point): Point = matrix.operate(doubleArrayOf(p.x, p.y, p.z, 1.0))
            .let { Point.xyz(it[0], it[1], it[2]) }

    fun andThen(a: Transform): Transform = ofMatrix(a.matrix.multiply(this@Transform.matrix))

    fun at(origin: Point): Transform = Translate(-origin.toVector()).andThen(this).andThen(Translate(origin.toVector()))

    fun invert(): Option<Transform> = QRDecomposition(matrix).solver.run { optionWhen(isNonSingular) { ofMatrix(inverse) } }

    companion object {

        fun ofMatrix(m: RealMatrix): Transform = object : Transform {
            override val matrix: RealMatrix = m
        }

        fun fromMatrixJson(json: JsonElement): Transform =
            json.array.map { it.array.map { it.double }.toDoubleArray() }.toTypedArray()
                    .let { ofMatrix(MatrixUtils.createRealMatrix(it)) }

        val Identity = ofMatrix(MatrixUtils.createRealIdentityMatrix(4))
    }
}

fun Transform.toMatrixJson(): JsonElement = jsonArray(matrix.data.map { jsonArray(it.asIterable()) })
