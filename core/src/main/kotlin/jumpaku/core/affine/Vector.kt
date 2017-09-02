package jumpaku.core.affine

import io.vavr.collection.Array
import io.vavr.control.Option
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import jumpaku.core.json.prettyGson
import jumpaku.core.util.divOption


operator fun Double.times(v: Vector): Vector = v.times(this)

data class Vector(val x: Double = 0.0, val y: Double = 0.0, val z : Double = 0.0) {

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

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): VectorJson = VectorJson(this)

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



data class VectorJson(private val x: Double, private val y: Double, private val z: Double){

    constructor(vector: Vector) : this(vector.x, vector.y, vector.z)

    fun vector(): Vector = Vector(x, y, z)
}
