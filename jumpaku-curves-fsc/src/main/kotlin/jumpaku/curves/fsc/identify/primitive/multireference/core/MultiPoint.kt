package jumpaku.curves.fsc.identify.primitive.multireference.core


import jumpaku.commons.control.None
import jumpaku.commons.control.Option
import jumpaku.commons.control.Some
import jumpaku.commons.control.orDefault
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Lerpable
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.transform.Transform


data class MultiPoint(val points: List<Point>) : Lerpable<MultiPoint>, List<Point> by points {

    constructor(vararg points: Point) : this(points.asList())

    init {
        require(isNotEmpty()) { "points must be not empty" }
    }

    val multiplicity: Int = size

    fun vertex(): Option<Point> = findVertex(points).map { it.first }

    fun isPossible(p: Point): Grade = findVertex(points + p).map { it.second }.orDefault(Grade.FALSE)

    /*fun isNecessary(p: Point): Grade {
        return p.isNecessary()
    }*/

    /**
     * Computes affine combination of conic fuzzy points.
     * @param terms sequence of pairs of coefficient and conic fuzzy point.
     * @return affine combination of conic fuzzy points
     */
    override fun lerp(terms: List<Pair<Double, MultiPoint>>): MultiPoint {
        require(terms.all { it.second.multiplicity == multiplicity }) { "points size mismatch" }
        return MultiPoint(points.indices.map { i -> points[i].lerp(terms.map { (t, m) -> t to m.points[i] }) })
    }

    override fun lerp(t: Double, p: MultiPoint): MultiPoint {
        require(multiplicity == p.multiplicity) { "points size mismatch" }
        return MultiPoint(points.zip(p.points) { a, b -> a.lerp(t, b) })
    }

    /**
     * @return A*p (crisp point)
     */
    fun transform(a: Transform): MultiPoint = MultiPoint(points.map { a(it) })

    companion object {

        /**
         *  finds a vertex that maximizes objective function by Nelder-Mead Method
         */
        fun findVertex(points: List<Point>): Option<Pair<Point, Grade>> {
            fun objective(p: Point): Double = points.map { q ->
                val d = q.dist(p)
                val value = d / q.r
                when {
                    value.isFinite() -> 1 - value
                    (1.0 / d).isFinite() -> Double.NEGATIVE_INFINITY
                    else -> 1.0
                }
            }.min()!!

            val crisps = points.filterNot { (1.0 / it.r).isFinite() }
            if (crisps.size == 1) return Some(crisps[0] to Grade.clamped(objective(crisps[0])))
            if (crisps.size > 1) return None

            val alpha = 1.0
            val gamma = 2.0
            val rho = 0.5
            val sigma = 0.5

            val initial = points.minBy { it.r }!!
            val simplex = listOf(
                    initial,
                    initial + Vector.I * initial.r,
                    initial + Vector.J * initial.r,
                    initial + Vector.K * initial.r
            ).map { it.toCrisp() }.map { it to objective(it) }.let { ArrayList(it) }

            repeat(50) {
                simplex.sortBy { it.second }
                val (x0, x1, x2, x3) = simplex.map { it.first }
                val (f0, f1, f2, f3) = simplex.map { it.second }
                val xCentroid = x3.lerp(1 / 3.0 to x1, 1 / 3.0 to x2)
                val xReflect = xCentroid.lerp(-alpha, x0)
                val fReflect = objective(xReflect)
                if (f3 > fReflect && fReflect >= f1) { // Reflection
                    simplex[0] = xReflect to fReflect
                } else if (fReflect >= f3) { // Expansion
                    val xExpand = xCentroid.lerp(-gamma, x0)
                    val fExpand = objective(xExpand)
                    simplex[0] = if (fExpand >= fReflect) xExpand to fExpand
                    else xReflect to fReflect
                } else if (f1 > fReflect) { // Contraction
                    val xContract = xCentroid.lerp(rho, x0)
                    val fContract = objective(xContract)
                    if (fContract >= f0) simplex[0] = xContract to fContract
                    else (0..2).forEach { i ->
                        val x = x3.lerp(sigma, x0)
                        simplex[i] = x to objective(x)
                    }
                }
            }
            return Some(simplex.last().first to Grade.clamped(simplex.last().second))
        }
    }
}

