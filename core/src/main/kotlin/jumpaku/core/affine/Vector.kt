package jumpaku.core.affine

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.collection.Array
import io.vavr.control.Option
import io.vavr.control.Try
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

    fun normalize(): Option<Vector> = this.divOption(length())

    fun isZero(eps: Double = 0.0): Boolean = square() <= eps*eps || normalize().isEmpty

    fun resize(l: Double): Option<Vector> = normalize().map { it*l }

    fun dot(v: Vector): Double = vector.dotProduct(Vector3D(v.x, v.y, v.z))

    fun square(): Double = vector.normSq

    fun length(): Double = vector.norm

    fun cross(v: Vector): Vector = Vector(vector.crossProduct(Vector3D(v.x, v.y, v.z)))

    fun angle(v: Vector): Double = Vector3D.angle(vector, Vector3D(v.x, v.y, v.z))

    fun toArray(): Array<Double> = Array.of(x, y, z)

    companion object {

        val I: Vector = Vector(x = 1.0)

        val J: Vector = Vector(y = 1.0)

        val K: Vector = Vector(z = 1.0)

        val Zero: Vector = Vector()

        fun fromJson(json: JsonElement): Option<Vector> {
            return Try.ofSupplier { Vector(json["x"].double, json["y"].double, json["z"].double) }.toOption()
        }
    }
}
