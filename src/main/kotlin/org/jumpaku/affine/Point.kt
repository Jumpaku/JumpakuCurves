package org.jumpaku.affine

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonParseException
import org.apache.commons.math3.util.FastMath
import org.jumpaku.fuzzy.Grade
import org.jumpaku.fuzzy.Membership
import org.jumpaku.json.prettyGson

interface Point : Membership<Point, Crisp>, Divisible<Point> {

    fun toVector(): Vector

    fun toCrisp(): Crisp

    val x: Double get() = toVector().x

    val y: Double get() = toVector().y

    val z: Double get() = toVector().z

    val r: Double

    operator fun component1(): Double = x

    operator fun component2(): Double = y

    operator fun component3(): Double = z

    operator fun component4(): Double = r

    override fun membership(p: Crisp): Grade{
        val d = toCrisp().dist(p)
        return if (java.lang.Double.isFinite(d / r)) {
            Grade(Grade.clamp(1.0 - d / r))
        }
        else {
            Grade(equals(toCrisp(), p))
        }
    }

    override fun possibility(p: Point): Grade{
        val d = toCrisp().dist(p.toCrisp())
        return if (!java.lang.Double.isFinite(d / (r + p.r))) {
            Grade(equals(toCrisp(), p.toCrisp()))
        }
        else {
            Grade(Grade.clamp(1 - d / (r + p.r)))
        }
    }

    override fun necessity(p: Point): Grade{
        val d = toCrisp().dist(p.toCrisp())
        return when {
            !java.lang.Double.isFinite(d / (r + p.r)) ->
                Grade(equals(toCrisp(), p.toCrisp()))
            d < p.r ->
                Grade(Grade.clamp(minOf(1 - (r - d) / (r + p.r), 1 - (r + d) / (r + p.r))))
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

    companion object {

        fun x(x: Double): Crisp = Crisp(x = x)

        fun xr(x: Double, r: Double): Fuzzy = Fuzzy(x = x, r = r)

        fun xy(x: Double, y: Double): Crisp = Crisp(x = x, y = y)

        fun xyr(x: Double, y: Double, r: Double): Fuzzy = Fuzzy(x = x, y = y, r = r)

        fun xyz(x: Double, y: Double, z: Double): Crisp = Crisp(x = x, y = y, z = z)

        fun xyzr(x: Double, y: Double, z: Double, r: Double): Fuzzy = Fuzzy(x = x, y = y, z = z, r = r)

        fun equals(p1: Crisp, p2: Crisp, eps: Double = 1.0e-10): Boolean = Vector.equals(p1.toVector(), p2.toVector(), eps)

        data class JsonPoint(val x: Double, val y: Double, val z: Double, val r:Double)

        fun toJson(p: Point): String = prettyGson.toJson(JsonPoint(p.x, p.y, p.z, p.r))

        fun fromJson(json: String): Point? {
            return try {
                val (x, y, z, r) = prettyGson.fromJson<JsonPoint>(json)
                Point.xyzr(x, y, z, r)
            } catch(e: Exception) {
                when (e) {
                    is IllegalArgumentException, is JsonParseException -> null
                    else -> throw e
                }
            }
        }
    }
}

class Fuzzy internal constructor(private val vector: Vector, override val r: Double) : Point {

    internal constructor(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0, r: Double = 0.0): this(Vector(x, y, z), r)

    init {
        if (r < 0){
            throw IllegalArgumentException("negative fuzziness r($r$)")
        }
    }

    override fun toVector(): Vector = vector

    override fun toCrisp(): Crisp = Crisp(vector)

    override fun toString(): String = Point.toJson(this)
}

class Crisp(private val vector: Vector) : Point {

    internal constructor(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): this(Vector(x, y, z))

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

    override fun toString(): String = Point.toJson(this)

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
