package org.jumpaku.affine

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.None
import io.vavr.API.Option
import io.vavr.control.Option
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.util.Precision
import org.jumpaku.json.prettyGson



operator fun Double.times(v: Vector): Vector = v.times(this)

class Vector private constructor(private val vector: Vector3D) {

    constructor(x: Double = 0.0, y: Double = 0.0, z : Double = 0.0) : this(Vector3D(x, y, z))

    val x: Double = vector.x

    val y: Double = vector.y

    val z: Double = vector.z

    operator fun component1(): Double = x

    operator fun component2(): Double = y

    operator fun component3(): Double = z

    operator fun plus(v: Vector): Vector = Vector(vector.add(v.vector))
    
    operator fun minus(v: Vector): Vector = plus(v.unaryMinus())
    
    operator fun times(s: Double): Vector = Vector(vector.scalarMultiply(s))

    operator fun unaryPlus(): Vector = this

    operator fun unaryMinus(): Vector = times(-1.0)

    fun minus(a: Double, v: Vector): Vector = minus(v.times(a))

    fun plus(a: Double, v: Vector): Vector = plus(v.times(a))

    fun normalize(): Vector = times(1.0 / length())

    fun resize(l: Double): Vector = times(l / length())

    fun dot(v: Vector): Double = vector.dotProduct(Vector3D(v.x, v.y, v.z))

    fun square(): Double = vector.normSq

    fun length(): Double = vector.norm

    fun cross(v: Vector): Vector = Vector(vector.crossProduct(Vector3D(v.x, v.y, v.z)))

    fun angle(v: Vector): Double = Vector3D.angle(vector, Vector3D(v.x, v.y, v.z))

    override fun toString(): String = VectorJson.toJson(this)

    companion object {

        fun add(a: Double, v1: Vector, b: Double, v2: Vector): Vector {
            return Vector(Vector3D(a, v1.vector, b, v2.vector))
        }

        fun equals(v1: Vector, v2: Vector, eps: Double = 1.0e-10): Boolean {
            return Precision.equals(v1.x, v2.x, eps)
                    && Precision.equals(v1.y, v2.y, eps)
                    && Precision.equals(v1.z, v2.z, eps)
        }

        val ZERO = Vector()
    }
}



data class VectorJson(val x: Double, val y: Double, val z: Double){

    companion object{

        fun toJson(v: Vector): String = prettyGson.toJson(VectorJson(v.x, v.y, v.z))

        fun fromJson(json: String): Option<Vector> {
            return try {
                val (x, y, z) = prettyGson.fromJson<VectorJson>(json)
                Option(Vector(x, y, z))
            } catch(e: Exception) {
                None()
            }
        }
    }
}
