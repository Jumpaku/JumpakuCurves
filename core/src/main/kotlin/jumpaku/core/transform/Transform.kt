package jumpaku.core.transform

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.jsonArray
import com.google.gson.JsonElement
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.geom.Point
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.QRDecomposition
import org.apache.commons.math3.linear.RealMatrix

interface Transform {

    val matrix: RealMatrix

    operator fun invoke(p: Point): Point = matrix.operate(doubleArrayOf(p.x, p.y, p.z, 1.0))
            .let { Point.xyz(it[0], it[1], it[2]) }

    fun andThen(a: Transform): Transform = ofMatrix(a.matrix.multiply(this@Transform.matrix))

    fun at(origin: Point): Transform = Translate(-origin.toVector()).andThen(this).andThen(Translate(origin.toVector()))

    fun invert(): Option<Transform> = QRDecomposition(matrix).solver.run { Option.`when`(isNonSingular) { ofMatrix(inverse) } }

    companion object {

        fun ofMatrix(m: RealMatrix): Transform = object : Transform {
            override val matrix: RealMatrix = m
        }

        fun fromMatrixJson(json: JsonElement): Option<Transform> = Try.ofSupplier {
            json.array.map { it.array.map { it.double }.toDoubleArray() }.toTypedArray()
                    .let { ofMatrix(MatrixUtils.createRealMatrix(it)) }
        }.toOption()

        val Identity = ofMatrix(MatrixUtils.createRealIdentityMatrix(4))
    }
}

fun Transform.toMatrixJson(): JsonElement = jsonArray(matrix.data.map { jsonArray(it.asIterable()) })
