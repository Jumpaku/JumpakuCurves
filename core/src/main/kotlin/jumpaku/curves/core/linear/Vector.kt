package jumpaku.curves.core.linear

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import jumpaku.curves.core.json.ToJson
import jumpaku.curves.core.json.jsonMap
import jumpaku.curves.core.json.map
import jumpaku.curves.core.util.Option
import jumpaku.curves.core.util.Result
import jumpaku.curves.core.util.result
import jumpaku.curves.core.util.tryDiv
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.MathArrays


infix operator fun Double.times(v: Vector): Vector = v.times(this)

sealed class Vector(override val size: Int) : AbstractList<Double>(), ToJson {

    fun mapIndexed(f: (Int, Double) -> Double): Vector = Array((0 until size).map { f(it, get(it)) })

    fun map(f: (Double) -> Double): Vector = mapIndexed { _, value -> f(value) }

    infix operator fun times(a: Double): Vector = when(this) {
        is Array -> map { it * a }
        is Sparse -> Sparse(size, data.mapValues { it.value * a })
    }

    infix operator fun div(divisor: Double): Result<Vector> = result {
        when (this) {
            is Array -> map { it.tryDiv(divisor).orThrow() }
            is Sparse -> Sparse(size, data.mapValues { it.value.tryDiv(divisor).orThrow() })
        }
    }

    operator fun unaryPlus(): Vector = this

    operator fun unaryMinus(): Vector = times(-1.0)

    infix operator fun plus(other: Vector): Vector {
        requireSameDimension(this, other)
        return when {
            this is Sparse && other is Sparse -> Sparse(size) {
                val result = mutableMapOf<Int, Double>()
                data.forEach { index, value -> result[index] = value }
                other.data.forEach { index, value -> result[index] = value + (result[index] ?: 0.0) }
                result.filterValues { 1.0.tryDiv(it).isSuccess }
            }
            else -> Array(toDoubleArray().zip(other.toDoubleArray(), Double::plus))
        }
    }

    infix operator fun minus(other: Vector): Vector {
        requireSameDimension(this, other)
        return plus(other.unaryMinus())
    }

    fun dot(other: Vector): Double {
        requireSameDimension(this, other)
        return when{
            this is Sparse && other is Sparse -> {
                val s = minOf(data, other.data, compareBy { it.size })
                val l = maxOf(other.data, data, compareBy { it.size })
                val keys = s.keys.intersect(l.keys).toList()
                if (keys.isEmpty()) 0.0 else MathArrays.linearCombination(
                        DoubleArray(keys.size) { data.getValue(keys[it]) },
                        DoubleArray(keys.size) { other.data.getValue(keys[it]) })
            }
            this is Sparse && other is Array -> {
                val keys = data.keys.toList()
                MathArrays.linearCombination(
                        DoubleArray(keys.size) { data.getValue(keys[it]) },
                        DoubleArray(keys.size) { other.data[keys[it]] })
            }
            this is Array && other is Sparse -> {
                val keys = other.data.keys.toList()
                MathArrays.linearCombination(
                        DoubleArray(keys.size) { data[keys[it]] },
                        DoubleArray(keys.size) { other.data.getValue(keys[it]) })
            }
            else -> MathArrays.linearCombination(toDoubleArray(), other.toDoubleArray())
        }
    }

    fun square(): Double = dot(this)

    fun norm(): Double = FastMath.sqrt(square())

    fun distSquare(other: Vector): Double {
        requireSameDimension(this, other)
        return minus(other).square()
    }

    fun dist(other: Vector): Double {
        requireSameDimension(this, other)
        return FastMath.sqrt(distSquare(other))
    }

    fun normalize(): Option<Vector> = div(norm()).value()

    fun asRow(): Matrix = when(this) {
        is Sparse -> Matrix.Sparse(1, size, data.mapKeys { (index, _) -> Matrix.Sparse.Key(0, index) })
        is Array -> Matrix.Array2D(arrayOf(toDoubleArray()))
    }

    fun asColumn(): Matrix = when(this) {
        is Sparse -> Matrix.Sparse(size, 1, data.mapKeys { (index, _) -> Matrix.Sparse.Key(index, 0) })
        is Array -> Matrix.Array2D(Array(size) { doubleArrayOf(get(it)) })
    }

    fun toDoubleArray(): DoubleArray = DoubleArray(size) { get(it) }

    override fun toString(): String = toJsonString()

    class Sparse(size: Int, data: Map<Int, Double>): Vector(size) {

        constructor(size: Int, builder: (size: Int) -> Map<Int, Double>): this(size, builder(size))

        val data: Map<Int, Double> = data.toMap()

        override operator fun get(index: Int): Double = data[index] ?: 0.0

        override fun toJson(): JsonElement = jsonObject(
                "type" to "Sparse".toJson(),
                "size" to size.toJson(),
                "data" to jsonMap(data.map { (k, v) -> k.toJson() to v.toJson() }.toMap()))
    }

    class Array(data: List<Double>): Vector(data.size) {

        constructor(data: DoubleArray): this(data.toList())

        val data: List<Double> = data.toList()

        override fun toJson(): JsonElement = jsonObject(
                "type" to "Array".toJson(),
                "data" to jsonArray(this))

        override operator fun get(index: Int): Double = data[index]
    }

    companion object {

        fun fromJson(json: JsonElement): Vector = when(json["type"].string) {
            "Sparse" -> Sparse(json["size"].int, json["data"].map.map { (k, v) -> k.int to v.double }.toMap())
            "Array" -> Array(json["data"].array.map { it.double })
            else -> error("invalid vector type")
        }

        fun zeros(size: Int): Vector = Sparse(size, emptyMap())

        fun ones(size: Int): Vector = Array(DoubleArray(size) { 1.0 })
    }

    private fun requireSameDimension(v0: Vector, v1: Vector) =
        require(v0.size == v1.size) { "Dimension mismatch ${v0.size} != ${v1.size}" }
}
