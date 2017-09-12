package jumpaku.core.affine

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.collection.Array
import io.vavr.control.Option
import jumpaku.core.json.ToJson
import jumpaku.core.util.divOption
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D


operator fun Double.times(v: Vector): Vector = v.times(this)

data class Vector(val x: Double = 0.0, val y: Double = 0.0, val z : Double = 0.0): ToJson {

    private constructor(vector: Vector3D) : this(vector.x, vector.y, vector.z)

    private val vector: Vector3D = Vector3D(x, y, z)

    operator fun plus(v: Vector): Vector = Vector(vector.add(v.vector))
    
    operator fun minus(v: Vector): Vector = Vector(vector.subtract(v.vector))

    operator fun times(s: Double): Vector = Vector(vector.scalarMultiply(s))

    operator fun div(divisor: Double): Vector = Vector(vector.scalarMultiply(1 / divisor))

    infix fun divOption(divisor: Double): Option<Vector> {
        return Option.`when`(toArray().all { it.divOption(divisor).isDefined }, this/divisor)
    }

    operator fun unaryPlus(): Vector = this

    operator fun unaryMinus(): Vector = -1.0*this

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("x" to x, "y" to y, "z" to z)

    fun normalize(): Vector {
        require((1/length()).isFinite()) { "$this close to zero" }
        return this/length()
    }

    fun resize(l: Double): Vector = l*normalize()

    fun dot(v: Vector): Double = vector.dotProduct(Vector3D(v.x, v.y, v.z))

    fun square(): Double = vector.normSq

    fun length(): Double = vector.norm

    fun cross(v: Vector): Vector = Vector(vector.crossProduct(Vector3D(v.x, v.y, v.z)))

    fun angle(v: Vector): Double = Vector3D.angle(vector, Vector3D(v.x, v.y, v.z))

    fun toArray(): Array<Double> = Array.of(x, y, z)
}

val JsonElement.vector: Vector get() = Vector(this["x"].double, this["y"].double, this["z"].double)