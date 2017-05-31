package org.jumpaku.affine

import com.github.salomonbrys.kotson.fromJson
import io.vavr.API.*
import io.vavr.control.Option
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

    override fun possibility(u: Point): Grade{
        val d = toCrisp().dist(u.toCrisp())
        return if (!java.lang.Double.isFinite(d / (r + u.r))) {
            Grade(equals(toCrisp(), u.toCrisp()))
        }
        else {
            Grade(Grade.clamp(1 - d / (r + u.r)))
        }
    }

    override fun necessity(u: Point): Grade{
        val d = toCrisp().dist(u.toCrisp())
        return when {
            !java.lang.Double.isFinite(d / (r + u.r)) ->
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

    companion object {

        fun x(x: Double): Crisp = Crisp(x, 0.0, 0.0)

        fun xr(x: Double, r: Double): Fuzzy = Fuzzy(x, 0.0, 0.0, r)

        fun xy(x: Double, y: Double): Crisp = Crisp(x, y)

        fun xyr(x: Double, y: Double, r: Double): Fuzzy = Fuzzy(x, y, 0.0, r)

        fun xyz(x: Double, y: Double, z: Double): Crisp = Crisp(x, y, z)

        fun xyzr(x: Double, y: Double, z: Double, r: Double): Fuzzy = Fuzzy(x, y, z, r)

        fun equals(p1: Crisp, p2: Crisp, eps: Double = 1.0e-10): Boolean = Vector.equals(p1.toVector(), p2.toVector(), eps)
    }
}


data class PointJson(private val x: Double, private val y: Double, private val z: Double, private val r:Double){

    fun point() = Point.xyzr(x, y, z, r)

    companion object{

        fun toJson(p: Point): String = prettyGson.toJson(PointJson(p.x, p.y, p.z, p.r))

        fun fromJson(json: String): Option<Point> {
            return try {
                Option(prettyGson.fromJson<PointJson>(json).point())
            } catch(e: Exception) {
                None()
            }
        }
    }
}



class Fuzzy(private val crisp: Crisp, override val r: Double) : Point {

    constructor(x: Double, y: Double, z: Double, r: Double): this(Crisp(x, y, z), r)

    constructor(vector: Vector, r: Double) : this(Crisp(vector), r)

    init {
        if (r < 0){
            throw IllegalArgumentException("negative fuzziness r($r$)")
        }
    }

    override fun toVector(): Vector = crisp.toVector()

    override fun toCrisp(): Crisp = crisp

    override fun toString(): String = PointJson.toJson(this)
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

    override fun toString(): String = PointJson.toJson(this)

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
