package jumpaku.curves.core.geom

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import jumpaku.curves.core.transform.Transform
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.json.ToJson
import jumpaku.curves.core.util.*
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.MathArrays
import org.apache.commons.math3.util.Precision
import kotlin.math.abs


data class Point(val x: Double, val y: Double, val z: Double, val r: Double = 0.0) :
        Lerpable<Point>, ToJson {

    constructor(v: Vector, r: Double = 0.0): this(v.x, v.y, v.z, r)

    init {
        require(r >= -0.0) { "r($r) must be positive"}
        require(x.isFinite() && y.isFinite() && z.isFinite()) { "($x, $y, $z) must be finite" }
    }

    fun toCrisp(): Point = copy(r = 0.0)

    fun toVector(): Vector = Vector(x, y, z)

    fun toDoubleArray(): DoubleArray = toVector().toDoubleArray()

    fun isPossible(u: Point): Grade = dist(u).tryDiv(r + u.r)
            .tryMap { Grade.clamped(1 - it) }.value()
            .orDefault(Grade(1.0.tryDiv(this.dist(u)).isFailure))

    fun isNecessary(u: Point): Grade {
        val d = this.dist(u)
        return d.tryDiv(r + u.r)
                .tryMap { if (d < u.r) Grade.clamped(1 - (r + d) / (r + u.r)) else Grade.FALSE }.value()
                .orDefault(Grade(isCloseTo(this, u)))
    }

    /**
     * Computes affine combination of conic fuzzy points.
     * @param terms sequence of pairs of coefficient and conic fuzzy point.
     * @return affine combination of conic fuzzy points
     */
    override fun lerp(vararg terms: Pair<Double, Point>): Point {
        val cs = terms.map { it.first }
        val c0 = 1 - sum(cs)
        val vs = terms.map { it.second.toVector() }
        val v = linearCombination(*(cs.zip(vs) + (c0 to this.toVector())).toTypedArray())
        val rs = terms.map { it.second.r }
        val r = MathArrays.linearCombination((cs + c0).map { abs(it) }.toDoubleArray(), (rs + this.r).toDoubleArray())
        return Point(v, r)
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("x" to x, "y" to y, "z" to z, "r" to r)

    private fun isCloseTo(p1: Point, p2: Point, eps: Double = 1.0e-10): Boolean =
            Precision.equals(p1.distSquare(p2), 0.0, eps*eps)

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

    fun dist(line: Line): Double = FastMath.sqrt(distSquare(line))

    fun distSquare(line: Line): Double = distSquare(projectTo(line))

    fun dist(plane: Plane): Double = FastMath.sqrt(distSquare(plane))

    fun distSquare(plane: Plane): Double = distSquare(projectTo(plane))

    fun projectTo(line: Line): Point {
        val l = org.apache.commons.math3.geometry.euclidean.threed.Line(
                Vector3D(line.p0.x, line.p0.y, line.p0.z),
                Vector3D(line.p1.x, line.p1.y, line.p1.z),
                0.0)
        return l.toSpace(l.toSubSpace(Vector3D(x, y, z))).let { Point(it.x, it.y, it.z) }
    }

    fun projectTo(plane: Plane): Point {
        val p = org.apache.commons.math3.geometry.euclidean.threed.Plane(
                Vector3D(plane.p0.x, plane.p0.y, plane.p0.z),
                Vector3D(plane.p1.x, plane.p1.y, plane.p1.z),
                Vector3D(plane.p2.x, plane.p2.y, plane.p2.z),
                0.0)
        return p.toSpace(p.toSubSpace(Vector3D(x, y, z))).let { Point(it.x, it.y, it.z) }
    }

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
    fun volume(p1: Point, p2: Point, p3: Point): Double =
            FastMath.abs((this - p1).cross(this - p2).dot(this - p3) / 6)

    /**
     * @param p1
     * @param p2
     * @return (p1-this)x(p2-this)/|(p1-this)x(p2-this)|
     */
    fun normal(p1: Point, p2: Point): Result<Vector> = (p1 - this).cross(p2 - this).normalize().tryMapFailure {
        IllegalArgumentException("normal for undefined plane")
    }

    /**
     * @return A*p (crisp point)
     */
    fun transform(a: Transform): Point = a(this)

    companion object {

        val origin: Point = Point(0.0, 0.0, 0.0)

        fun x(x: Double): Point = Point(x, 0.0, 0.0, 0.0)

        fun xr(x: Double, r: Double): Point = Point(x, 0.0, 0.0, r)

        fun xy(x: Double, y: Double): Point = Point(x, y, 0.0, 0.0)

        fun xyr(x: Double, y: Double, r: Double): Point = Point(x, y, 0.0, r)

        fun xyz(x: Double, y: Double, z: Double): Point = Point(x, y, z, 0.0)

        fun xyzr(x: Double, y: Double, z: Double, r: Double): Point = Point(x, y, z, r)

        fun fromJson(json: JsonElement): Point = Point(json["x"].double, json["y"].double, json["z"].double, json["r"].double)
    }
}
