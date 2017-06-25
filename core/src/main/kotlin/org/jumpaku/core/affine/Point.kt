package org.jumpaku.core.affine

import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Precision
import org.jumpaku.core.fuzzy.Grade
import org.jumpaku.core.fuzzy.Membership
import org.jumpaku.core.json.prettyGson

sealed class Point : Membership<Point, Crisp>, Divisible<Point> {

    abstract fun toVector(): Vector

    abstract fun toCrisp(): Crisp

    val x: Double get() = toVector().x

    val y: Double get() = toVector().y

    val z: Double get() = toVector().z

    abstract val r: Double

    operator fun component1(): Double = x

    operator fun component2(): Double = y

    operator fun component3(): Double = z

    operator fun component4(): Double = r

    override fun membership(p: Crisp): Grade{
        val d = toCrisp().dist(p)
        return if ((d / r).isFinite()) {
            Grade(Grade.clamp(1.0 - d / r))
        }
        else {
            Grade(equals(toCrisp(), p))
        }
    }

    override fun possibility(u: Point): Grade{
        val d = toCrisp().dist(u.toCrisp())
        return if (!(d / (r + u.r)).isFinite()) {
            Grade(equals(toCrisp(), u.toCrisp()))
        }
        else {
            Grade(Grade.clamp(1 - d / (r + u.r)))
        }
    }

    override fun necessity(u: Point): Grade{
        val d = toCrisp().dist(u.toCrisp())
        return when {
            !(d / (r + u.r)).isFinite() ->
                Grade(equals(toCrisp(), u.toCrisp()))
            d < u.r ->
                Grade(Grade.clamp(minOf(1 - (r - d) / (r + u.r), 1 - (r + d) / (r + u.r))))
            else ->
                Grade.FALSE
        }
    }

    /**
     * @param t
     * @param p
     * @return this+t*(p-this) = (1-t)*this + t*p
     */
    override fun divide(t: Double, p: Point): Point {
        return Fuzzy(toVector().times(1 - t).plus(t, p.toVector()),
                FastMath.abs(1 - t) * r + FastMath.abs(t) * p.r)
    }

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): PointJson = PointJson(this)

    private fun equals(p1: Crisp, p2: Crisp, eps: Double = 1.0e-10): Boolean {
        return Precision.equals(p1.x, p2.x, eps)
                && Precision.equals(p1.y, p2.y, eps)
                && Precision.equals(p1.z, p2.z, eps)
    }

    companion object {

        fun x(x: Double): Crisp = Crisp(x, 0.0, 0.0)

        fun xr(x: Double, r: Double): Fuzzy = Fuzzy(x, 0.0, 0.0, r)

        fun xy(x: Double, y: Double): Crisp = Crisp(x, y)

        fun xyr(x: Double, y: Double, r: Double): Fuzzy = Fuzzy(x, y, 0.0, r)

        fun xyz(x: Double, y: Double, z: Double): Crisp = Crisp(x, y, z)

        fun xyzr(x: Double, y: Double, z: Double, r: Double): Fuzzy = Fuzzy(x, y, z, r)
    }
}


data class PointJson(private val x: Double, private val y: Double, private val z: Double, private val r:Double){

    constructor(point: Point) : this(point.x, point.y, point.z, point.r)

    fun point() = Point.xyzr(x, y, z, r)
}



class Fuzzy(private val crisp: Crisp, override val r: Double) : Point() {

    constructor(x: Double, y: Double, z: Double, r: Double): this(Crisp(x, y, z), r)

    constructor(vector: Vector, r: Double) : this(Crisp(vector), r)

    init {
        if (r < 0){
            throw IllegalArgumentException("negative fuzziness r($r$)")
        }
    }

    override fun toVector(): Vector = crisp.toVector()

    override fun toCrisp(): Crisp = crisp
}

class Crisp(private val vector: Vector) : Point() {

    constructor(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): this(Vector(x, y, z))

    override val r = 0.0

    /**
     * @param v
     * @return this + v
     */
    operator fun plus(v: Vector): Crisp = Crisp(toVector().plus(v))

    /**
     * @param p
     * @return this - p
     */
    operator fun minus(p: Crisp): Vector = toVector().minus(p.toVector())

    override fun toVector(): Vector = vector

    override fun toCrisp(): Crisp = this

    /**
     * @return distance |p - this|
     */
    fun dist(p: Crisp): Double = minus(p).length()

    fun distSquare(p: Crisp): Double = minus(p).square()

    /**
     * @param p1
     * @param p2
     * @return area of a triangle (this, p1, p2)
     */
    fun area(p1: Crisp, p2: Crisp): Double = FastMath.abs(minus(p1).cross(minus(p2)).length() / 2)

    /**
     * @param p1
     * @param p2
     * @param p3
     * @return volume of a Tetrahedron (this, p1, p2, p3)
     */
    fun volume(p1: Crisp, p2: Crisp, p3: Crisp): Double = FastMath.abs(minus(p1).cross(minus(p2)).dot(minus(p3)) / 6)

    /**
     * @param p1
     * @param p2
     * @return (p1-this)x(p2-this)/|(p1-this)x(p2-this)|
     */
    fun normal(p1: Crisp, p2: Crisp): Vector = p1.minus(this).cross(p2.minus(this)).normalize()

    fun transform(a: Transform): Crisp = a.invoke(this)
}
