package jumpaku.curves.core.geom

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.commons.control.Result
import jumpaku.commons.control.result
import jumpaku.commons.json.ToJson
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.util.MathArrays


operator fun Double.times(v: Vector): Vector = v.times(this)

fun linearCombination(vararg terms: Pair<Double, Vector>): Vector {
    if (terms.isEmpty()) return Vector.Zero
    val cs = terms.map { it.first }.toDoubleArray()
    val xs = terms.map { it.second.x }.toDoubleArray()
    val ys = terms.map { it.second.y }.toDoubleArray()
    val zs = terms.map { it.second.z }.toDoubleArray()
    val x = MathArrays.linearCombination(cs, xs)
    val y = MathArrays.linearCombination(cs, ys)
    val z = MathArrays.linearCombination(cs, zs)
    return Vector(x, y, z)
}

data class Vector(val x: Double = 0.0, val y: Double = 0.0, val z : Double = 0.0): ToJson {

    private constructor(vector: Vector3D) : this(vector.x, vector.y, vector.z)

    init {
        require(x.isFinite() && y.isFinite() && z.isFinite()) { "($x, $y, $z) must be finite" }
    }

    private val vector: Vector3D = Vector3D(x, y, z)

    operator fun plus(v: Vector): Vector = Vector(vector.add(v.vector))
    
    operator fun minus(v: Vector): Vector = Vector(vector.subtract(v.vector))

    operator fun times(s: Double): Vector = Vector(vector.scalarMultiply(s))

    private fun isDivisibleBy(divisor: Double): Boolean = toDoubleArray().all { (it/divisor).isFinite() }

    operator fun div(divisor: Double): Result<Vector> = result {
        if (isDivisibleBy(divisor)) Vector(vector.scalarMultiply(1 / divisor))
        else throw ArithmeticException("divide by zero")
    }

    operator fun unaryPlus(): Vector = this

    operator fun unaryMinus(): Vector = -1.0*this

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("x" to x, "y" to y, "z" to z)

    fun normalize(): Result<Vector> = div(length()).tryMapFailure { IllegalStateException("$this is close to zero.") }

    fun resize(l: Double): Result<Vector> = normalize().tryMap { it.times(l) }

    fun dot(v: Vector): Double = vector.dotProduct(Vector3D(v.x, v.y, v.z))

    fun square(): Double = vector.normSq

    fun length(): Double = vector.norm

    fun cross(v: Vector): Vector = Vector(vector.crossProduct(Vector3D(v.x, v.y, v.z)))

    fun angle(v: Vector): Double = Vector3D.angle(vector, Vector3D(v.x, v.y, v.z))

    fun toDoubleArray(): DoubleArray = doubleArrayOf(x, y, z)

    companion object {

        val I: Vector = Vector(x = 1.0)

        val J: Vector = Vector(y = 1.0)

        val K: Vector = Vector(z = 1.0)

        val Zero: Vector = Vector()

        fun fromJson(json: JsonElement): Vector = Vector(json["x"].double, json["y"].double, json["z"].double)
    }
}
