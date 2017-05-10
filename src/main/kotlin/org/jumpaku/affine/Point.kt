package org.jumpaku.affine

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonParseException
import org.apache.commons.math3.util.FastMath
import org.jumpaku.fuzzy.Grade
import sun.plugin2.util.PojoUtil.toJson
import org.jumpaku.fuzzy.Membership
import org.jumpaku.json.prettyGson

interface Point : Membership<Point, Crisp>, Divisible<Point> {

    fun toVector(): Vector

    fun toCrisp(): Crisp

    val x: Double
        get() = toVector().x

    val y: Double
        get() = toVector().y

    val z: Double
        get() = toVector().z

    val r: Double

    override fun membership(p: Crisp): Grade{
        val d = toCrisp().dist(p)
        return if (java.lang.Double.isFinite(d / r))
            Grade(Grade.clamp(1.0 - d / r))
        else
            Grade(Vector.equals(toVector(), p.toVector(), 1.0e-10))
    }

    override fun possibility(p: Point): Grade{
        val ra = r
        val rb = p.r
        val d = toCrisp().dist(p.toCrisp())
        return if (!java.lang.Double.isFinite(d / (ra + rb)))
            Grade(Vector.equals(toVector(), p.toVector(), 1.0e-10))
        else
            Grade(Grade.clamp(1 - d / (ra + rb)))
    }

    override fun necessity(p: Point): Grade{
        val ra = r
        val rb = p.r
        val d = toCrisp().dist(p.toCrisp())
        return if (!java.lang.Double.isFinite(d / (ra + rb)))
            Grade(Vector.equals(toVector(), p.toVector(), 1.0e-10))
        else if (d < rb)
            Grade(Grade.clamp(
                    FastMath.min(1 - (ra - d) / (ra + rb), 1 - (ra + d) / (ra + rb))))
        else
            Grade.FALSE
    }

    /**
     * @param t
     * @param p
     * @return this+t*(p-this) = (1-t)*this + t*p
     */
    override fun divide(t: Double, p: Point): Point {
        return Fuzzy(FastMath.abs(1 - t) * r + FastMath.abs(t) * p.r,
                toVector().times(1 - t).plus(t, p.toVector()))
    }

    companion object {

        data class JsonPoint(val r:Double, val x: Double, val y: Double, val z: Double)

        fun toJson(v: Point): String = prettyGson.toJson(JsonPoint(v.r, v.x, v.y, v.z))

        fun fromJson(json: String): Point? {
            return try {
                val v = prettyGson.fromJson<JsonPoint>(json)
                Fuzzy(v.r, v.x, v.y, v.z)
            } catch(e: Exception) {
                when (e) {
                    is IllegalArgumentException, is JsonParseException -> null
                    else -> throw e
                }
            }
        }
    }
}


class Fuzzy(override val r: Double, private val vector: Vector) : Point {

    constructor(r: Double, x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): this(r, Vector(x, y, z))

    override fun toVector(): Vector = vector

    override fun toCrisp(): Crisp = Crisp(vector)

    override fun toString(): String = toJson(this)
}

class Crisp(private val vector: Vector) : Point {

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

    override fun toString(): String = toJson(this)

    /**
     * @return distance |p - this|
     */
    fun dist(p: Crisp): Double = minus(p).length()

    /**
     * distance between this point and line ab.
     * @param a
     * @param b
     * @return
     */
    /*fun dist(a: Crisp, b: Crisp): Double {
        val p = this
        val ap = p.diff(a)
        val ab = b.diff(a)
        val h = a.move(ab.scale(ap.dot(ab) / ab.square()))
        return p.dist(h)
    }*/

    fun distSquare(p: Crisp): Double = minus(p).square()

    /**
     * @param p1
     * @param p2
     * @return area of a triangle (this, p1, p2)
     */
    fun area(p1: Crisp, p2: Crisp): Double = minus(p1).cross(minus(p2)).length() / 2.0

    /**
     * @param p1
     * @param p2
     * @param p3
     * @return volume of a Tetrahedron (this, p1, p2, p3)
     */
    fun volume(p1: Crisp, p2: Crisp, p3: Crisp): Double = minus(p1).cross(minus(p2)).dot(minus(p3)) / 6.0

    /**
     * @param p1
     * @param p2
     * @return (p1-this)x(p2-this)/|(p1-this)x(p2-this)|
     */
    fun normal(p1: Crisp, p2: Crisp): Vector = p1.minus(this).cross(p2.minus(this)).normalize()

    fun transform(a: Transform): Crisp = a.invoke(this)
}
