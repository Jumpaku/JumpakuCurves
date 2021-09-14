package jumpaku.curves.core.geom

import jumpaku.commons.control.Result
import jumpaku.commons.control.result
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D


operator fun Double.times(v: Vector): Vector = v.times(this)

data class Vector(val x: Double = 0.0, val y: Double = 0.0, val z: Double = 0.0) : Lerpable<Vector> {

    private constructor(vector: Vector3D) : this(vector.x, vector.y, vector.z)

    init {
        require(x.isFinite() && y.isFinite() && z.isFinite()) { "($x, $y, $z) must be finite" }
    }

    private val vector: Vector3D = Vector3D(x, y, z)

    operator fun plus(v: Vector): Vector = Vector(vector.add(v.vector))

    operator fun minus(v: Vector): Vector = Vector(vector.subtract(v.vector))

    operator fun times(s: Double): Vector = Vector(vector.scalarMultiply(s))

    private fun isDivisibleBy(divisor: Double): Boolean = toDoubleArray().all { (it / divisor).isFinite() }

    operator fun div(divisor: Double): Result<Vector> = result {
        if (isDivisibleBy(divisor)) Vector(vector.scalarMultiply(1 / divisor))
        else throw ArithmeticException("divide by zero")
    }

    operator fun unaryPlus(): Vector = this

    operator fun unaryMinus(): Vector = -1.0 * this

    fun normalize(): Result<Vector> = div(length()).tryMapFailure { IllegalStateException("$this is close to zero.") }

    fun resize(l: Double): Result<Vector> = normalize().tryMap { it.times(l) }

    fun dot(v: Vector): Double = vector.dotProduct(Vector3D(v.x, v.y, v.z))

    fun square(): Double = vector.normSq

    fun length(): Double = vector.norm

    fun cross(v: Vector): Vector = Vector(vector.crossProduct(Vector3D(v.x, v.y, v.z)))

    fun angle(v: Vector): Double = Vector3D.angle(vector, Vector3D(v.x, v.y, v.z))

    fun toDoubleArray(): DoubleArray = doubleArrayOf(x, y, z)

    override fun lerp(terms: List<Pair<Double, Vector>>): Vector {
        var s = Zero
        var c0 = 1.0
        for ((c, v) in terms) {
            s += c * v
            c0 -= c
        }
        return s + (this * c0)
    }

    override fun lerp(t: Double, p: Vector): Vector = Vector(
            x.lerp(t, p.x),
            y.lerp(t, p.y),
            z.lerp(t, p.z)
    )

    companion object {

        val I: Vector = Vector(x = 1.0)

        val J: Vector = Vector(y = 1.0)

        val K: Vector = Vector(z = 1.0)

        val Zero: Vector = Vector()

    }

}

