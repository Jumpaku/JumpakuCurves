package org.jumpaku.core.affine

import io.vavr.collection.Array
import org.apache.commons.math3.geometry.euclidean.threed.Line
import org.apache.commons.math3.geometry.euclidean.threed.Plane
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Precision
import org.jumpaku.core.fuzzy.Grade
import org.jumpaku.core.fuzzy.Membership
import org.jumpaku.core.json.prettyGson
import org.jumpaku.core.util.divOption

data class Point(val x: Double, val y: Double, val z: Double, val r: Double = 0.0) : Membership<Point, Point>, Divisible<Point> {

    constructor(v: Vector, r: Double = 0.0): this(v.x, v.y, v.z, r)

    fun toCrisp(): Point = copy(r = 0.0)

    fun toVector(): Vector = Vector(x, y, z)

    fun toArray(): Array<Double> = toVector().toArray()

    override fun membership(p: Point): Grade{
        return dist(p).divOption(r)
                .map { Grade.clamped(1 - it) }
                .getOrElse(Grade(equalsPosition(this, p)))
    }

    override fun isPossible(u: Point): Grade{
        return dist(u).divOption(r + u.r)
                .map { Grade.clamped(1 - it) }
                .getOrElse(Grade(equalsPosition(this, u)))
    }

    override fun isNecessary(u: Point): Grade{
        val d = this.dist(u)
        return d.divOption(r + u.r)
                .map { when {
                        it >= u.r -> Grade.FALSE
                        else -> Grade.clamped(minOf(1 - (r - d) / (r + u.r), 1 - (r + d) / (r + u.r)))
                } }
                .getOrElse(Grade(equalsPosition(this, u)))
    }

    /**
     * @param t
     * @param p
     * @return ((1-t)*this.vector + t*p.vector, |1-t|*this.r + |t|*p.r)
     */
    override fun divide(t: Double, p: Point): Point {
        return Point(toVector() * (1 - t) + t * p.toVector(),
                FastMath.abs(1 - t) * r + FastMath.abs(t) * p.r)
    }

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): PointJson = PointJson(this)

    private fun equalsPosition(p1: Point, p2: Point, eps: Double = 1.0e-10): Boolean {
        return Precision.equals(p1.distSquare(p2), 0.0, eps*eps)
    }

    /**
     * @param v
     * @return this + v (as a crisp point)
     */
    operator fun plus(v: Vector): Point = Point(toVector() + v)

    /**
     * @param v
     * @return this - v (as a crisp point)
     */
    operator fun minus(v: Vector): Point = this + (-v)

    /**
     * @param p
     * @return this - p
     */
    operator fun minus(p: Point): Vector = toVector() - p.toVector()


    /**
     * @return distance |p - this|
     */
    fun dist(p: Point): Double = FastMath.sqrt(distSquare(p))

    /**
     * @return distance |p - this|^2
     */
    fun distSquare(p: Point): Double = (this - p).square()

    fun dist(l: Line): Double = FastMath.sqrt(distSquare(l))

    fun distSquare(l: Line): Double = distSquare(projectTo(l))

    fun dist(p: Plane): Double = FastMath.sqrt(distSquare(p))

    fun distSquare(p: Plane): Double = distSquare(projectTo(p))

    fun projectTo(l: Line): Point = l.toSpace(l.toSubSpace(Vector3D(x, y, z))).let { Point(it.x, it.y, it.z) }

    fun projectTo(p: Plane): Point = p.toSpace(p.toSubSpace(Vector3D(x, y, z))).let { Point(it.x, it.y, it.z) }

    /**
     * @param p1
     * @param p2
     * @return area of a triangle (this, p1, p2)
     */
    fun area(p1: Point, p2: Point): Double = FastMath.abs((this - p1).cross(this - p2).length() / 2)

    /**
     * @param p1
     * @param p2
     * @param p3
     * @return volume of a Tetrahedron (this, p1, p2, p3)
     */
    fun volume(p1: Point, p2: Point, p3: Point): Double = FastMath.abs((this - p1).cross(this - p2).dot(this - p3) / 6)

    /**
     * @param p1
     * @param p2
     * @return (p1-this)x(p2-this)/|(p1-this)x(p2-this)|
     */
    fun normal(p1: Point, p2: Point): Vector = (p1 - this).cross(p2 - this).normalize()

    /**
     * @return A*p (crisp point)
     */
    fun transform(a: Affine): Point = a(this)

    companion object {

        fun x(x: Double): Point = Point(x, 0.0, 0.0, 0.0)

        fun xr(x: Double, r: Double): Point = Point(x, 0.0, 0.0, r)

        fun xy(x: Double, y: Double): Point = Point(x, y, 0.0, 0.0)

        fun xyr(x: Double, y: Double, r: Double): Point = Point(x, y, 0.0, r)

        fun xyz(x: Double, y: Double, z: Double): Point = Point(x, y, z, 0.0)

        fun xyzr(x: Double, y: Double, z: Double, r: Double): Point = Point(x, y, z, r)
    }
}


data class PointJson(private val x: Double, private val y: Double, private val z: Double, private val r:Double){

    constructor(point: Point) : this(point.x, point.y, point.z, point.r)

    fun point() = Point.xyzr(x, y, z, r)
}
