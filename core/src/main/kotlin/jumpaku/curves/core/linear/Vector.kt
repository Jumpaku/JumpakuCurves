package jumpaku.curves.core.linear

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.curves.core.json.ToJson
import jumpaku.curves.core.util.Option
import jumpaku.curves.core.util.Result
import jumpaku.curves.core.util.result
import jumpaku.curves.core.util.tryDiv
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.MathArrays


fun Double.times(v: Vector): Vector = v.times(this)

fun linearCombination(dimension: Int, vararg terms: Pair<Double, Vector>): Vector {
    if (terms.isEmpty()) return Vector.zeros(dimension)
    val cs = Vector(terms.map { it.first })
    return Vector((0 until dimension).map { i -> Vector(terms.map { it.second[i] }).dot(cs) })
}

class Vector private constructor(val vector: RealVector): AbstractList<Double>(), ToJson {

    constructor(array: DoubleArray): this(ArrayRealVector(array))

    constructor(values: List<Double>): this(values.toDoubleArray())

    override val size: Int = vector.dimension

    operator fun plus(other: Vector): Vector =  Vector(vector.add(other.vector))

    operator fun times(a: Double): Vector = Vector(vector.mapMultiply(a))

    operator fun unaryPlus(): Vector = this

    operator fun unaryMinus(): Vector = times(-1.0)

    operator fun minus(v: Vector): Vector = Vector(vector.subtract(v.vector))

    operator fun div(divisor: Double): Result<Vector> = result { Vector(vector.map { it.tryDiv(divisor).orThrow() }) }

    fun dot(v: Vector): Double = MathArrays.linearCombination(vector.toArray(), v.vector.toArray())

    fun square(): Double = dot(this)

    fun norm(): Double = FastMath.sqrt(square())

    fun distSquare(v: Vector): Double = minus(v).square()

    fun dist(v: Vector): Double = FastMath.sqrt(distSquare(v))

    fun unit(): Option<Vector> = div(norm()).value()

    fun map(f: (Double) -> Double): Vector = Vector(vector.map(f))

    fun asRow(): Matrix = Matrix.Array2D(arrayOf(toDoubleArray()))

    fun asColumn(): Matrix = Matrix.Array2D(Array(size) { doubleArrayOf(get(it)) })

    override operator fun get(index: Int): Double = vector.getEntry(index)

    fun toDoubleArray(): DoubleArray = DoubleArray(size) { get(it) }

    override fun toJson(): JsonElement = jsonObject("values" to jsonArray(this))

    override fun toString(): String = toJsonString()

    companion object {

        fun fromJson(json: JsonElement): Vector =
                Vector(ArrayRealVector(json["values"].array.map { it.double }.toDoubleArray()))

        fun zeros(size: Int): Vector = Vector(DoubleArray(size))

        fun ones(size: Int): Vector = Vector(DoubleArray(size) { 1.0 })
    }
}
