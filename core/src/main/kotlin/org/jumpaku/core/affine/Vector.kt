package org.jumpaku.core.affine

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.None
import io.vavr.API.Option
import io.vavr.control.Option
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.util.Precision
import org.jumpaku.core.json.prettyGson



operator fun Double.times(v: Vector): Vector = v.times(this)

data class Vector constructor(val x: Double = 0.0, val y: Double = 0.0, val z : Double = 0.0) {

    private constructor(vector: Vector3D) : this(vector.x, vector.y, vector.z)

    private val vector: Vector3D = Vector3D(x, y, z)

    operator fun plus(v: Vector): Vector = Vector(vector.add(v.vector))
    
    operator fun minus(v: Vector): Vector = plus(v.unaryMinus())
    
    operator fun times(s: Double): Vector = Vector(vector.scalarMultiply(s))

    operator fun unaryPlus(): Vector = this

    operator fun unaryMinus(): Vector = times(-1.0)

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): VectorJson = VectorJson(this)

    fun minus(a: Double, v: Vector): Vector = minus(v.times(a))

    fun plus(a: Double, v: Vector): Vector = plus(v.times(a))

    fun normalize(): Vector = times(1.0 / length())

    fun resize(l: Double): Vector = times(l / length())

    fun dot(v: Vector): Double = vector.dotProduct(Vector3D(v.x, v.y, v.z))

    fun square(): Double = vector.normSq

    fun length(): Double = vector.norm

    fun cross(v: Vector): Vector = Vector(vector.crossProduct(Vector3D(v.x, v.y, v.z)))

    fun angle(v: Vector): Double = Vector3D.angle(vector, Vector3D(v.x, v.y, v.z))
}



data class VectorJson(private val x: Double, private val y: Double, private val z: Double){

    constructor(vector: Vector) : this(vector.x, vector.y, vector.z)

    fun vector(): Vector = Vector(x, y, z)
}
