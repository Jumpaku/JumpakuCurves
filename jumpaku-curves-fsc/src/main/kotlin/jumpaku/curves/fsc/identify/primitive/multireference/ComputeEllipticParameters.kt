package jumpaku.curves.fsc.identify.primitive.multireference

import jumpaku.commons.control.result
import jumpaku.commons.math.divOrDefault
import jumpaku.commons.math.tryDiv
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.geom.*
import jumpaku.curves.core.transform.Calibrate
import jumpaku.curves.core.transform.Transform
import org.apache.commons.math3.analysis.solvers.BrentSolver
import kotlin.math.*

fun <C : Curve> computeGlobalEllipticParameters(fsc: ReparametrizedCurve<C>, generations: Int, nSamples: Int = 100, nDivisions: Int = 10): Pair<Double, List<Double>> {
    val original = fsc.originalCurve
    val fscReparametrizer = fsc.reparametrizer
    val divisions = fsc.sample(nDivisions)
            .map { it.run { copy(param = fscReparametrizer.toOriginal(param)) } }
    val samples = original.sample(nSamples)
    val begin = samples.first()
    val end = samples.last()
    val frontPoints = divisions
            .map { p -> computeLocalEllipticFar(fsc, samples, begin.param, p.param) }
    val backPoints = divisions
            .map { p -> computeLocalEllipticFar(fsc, samples, p.param, end.param) }
    val middlePoints = divisions.indices
            .map { i -> computeLocalEllipticFar(fsc, samples, frontPoints[i].param, backPoints[i].param) }
    val halfWeights = divisions.indices
            .map { i -> computeLocalEllipticWeight(frontPoints[i], middlePoints[i], backPoints[i], samples) }
    val globalHalfWeight = halfWeights.map { acos(it) * 2 }.average().let { cos(it / 2) }
    val localRepresentParams = divisions.indices.map { i ->
        computeLocalEllipticRepresentParams(fsc, globalHalfWeight, frontPoints[i], middlePoints[i], backPoints[i], generations)
    }
    val nFars = localRepresentParams[0].size
    val farParams = (0 until nFars).map { i -> localRepresentParams.map { it[i] }.average().coerceIn(original.domain) }
    return globalHalfWeight to farParams
}

fun <C : Curve> computeLocalEllipticRepresentParams(
        fsc: ReparametrizedCurve<C>,
        halfWeight: Double,
        front: ParamPoint,
        middle: ParamPoint,
        back: ParamPoint,
        generations: Int): List<Double> {
    class Elliptic(val affine: Transform) : Curve {
        override val domain: Interval = Interval(-2 * PI, 2 * PI)
        override fun evaluate(t: Double): Point = affine(Point.xy(sin(t), cos(t)))
    }

    val halfAngle = acos(halfWeight) * 2

    val nFars = (1 shl (generations + 2)) + 1
    val original = fsc.originalCurve
    val affine = result {
        Calibrate(
                Point.xy(sin(-halfAngle / 2), cos(-halfAngle / 2)) to front.point,
                Point.xy(0.0, 1.0) to middle.point,
                Point.xy(sin(halfAngle / 2), cos(halfAngle / 2)) to back.point,
                Point.xyz(0.0, 1.0, 1.0) to middle.point)
    }.orRecover {
        println("LINE")
        return original.domain.sample(nFars)
    }
    val fscReparametrizer = fsc.reparametrizer
    val (begin, end) = original.sample(2)
    val (l0, l1, l2, l3, l4) = listOf(begin, front, middle, back, end)
            .map { fscReparametrizer.toArcLengthRatio(it.param) }

    val elliptic = Elliptic(affine).let { e -> ReparametrizedCurve.of(e, e.domain.sample(761)) }
    val ellipticReparametrizer = elliptic.reparametrizer
    val (m1, m2, m3) = listOf(-halfAngle / 2, 0.0, halfAngle / 2).map { ellipticReparametrizer.toArcLengthRatio(it) }
    val m0 = m2.lerp((l2 - l0) / (l2 - l1), m1)
    val m4 = m2.lerp((l2 - l4) / (l2 - l3), m3)
    val eBegin = ellipticReparametrizer.toOriginal(m0.coerceIn(0.0, 1.0))
    val eEnd = ellipticReparametrizer.toOriginal(m4.coerceIn(0.0, 1.0))
    val lengths = Interval(eBegin, eEnd).sample(nFars)
            .map { ellipticReparametrizer.toArcLengthRatio(it.coerceIn(elliptic.originalCurve.domain)) }
            .map { (it - m0) / (m4 - m0) }
    return lengths.map { fscReparametrizer.toOriginal(it.coerceIn(0.0, 1.0)) }
}

fun <C : Curve> computeLocalEllipticFar(fsc: ReparametrizedCurve<C>, samples: List<ParamPoint>, tFront: Double, tBack: Double): ParamPoint {
    val original = fsc.originalCurve
    val ps = samples.filter { it.param in tFront..tBack }
            .let { listOf(ParamPoint(original(tFront), tFront)) + it + listOf(ParamPoint(original(tBack), tBack)) }
    val middle = ps.first().point.middle(ps.last().point)

    val areas = ps.map { it.point }.zipWithNext(middle::area)
            .fold(mutableListOf(0.0)) { acc, a ->
                acc.add(acc.last() + a); acc
            }
    val index = areas.indexOfLast { it < areas.last() / 2 }
    if (index < 0) return ParamPoint(original(tFront).middle(original(tBack)), tFront.middle(tBack))

    val t = BrentSolver(1.0e-6).solve(50, {
        val c = original(ps[index].param.lerp(it, ps[index + 1].param))
        val l = areas[index] + middle.area(ps[index].point, c)
        val r = areas.last() - areas[index + 1] + middle.area(ps[index + 1].point, c)
        l - r
    }, 0.0, 1.0)
    val tFar = ps[index].param.lerp(t, ps[index + 1].param)
    return ParamPoint(original(tFar), tFar)
}


fun computeLocalEllipticWeight(p0: ParamPoint, p1: ParamPoint, p2: ParamPoint, samples: List<ParamPoint>): Double {
    val (q0, t0) = p0
    val (q1, t1) = p1
    val (q2, t2) = p2
    val u0 = q0 - q1
    val u2 = q2 - q1
    val n = u0.cross(u2)
    val v = q2 - q0
    val data = samples.mapNotNull { (q, t) ->
        val u = q - q1
        when {
            t in t0..t2 && n.dot(u0.cross(u)) >= 0.0 && n.dot(u2.cross(u)) <= 0.0 -> 1.0 to 1.0
            t < t0 && n.dot(u0.cross(u)) <= 0.0 -> 1.0 to 1.0
            t > t2 && n.dot(u2.cross(u)) >= 0.0 -> 1.0 to 1.0
            //t !in t0..t2 && (n.dot(u0.cross(u)) <= 0.0 || n.dot(u2.cross(u)) >= 0.0) -> 1.0 to 1.0
            t in t0..t2 && n.dot(v.cross(u)) >= 0.0 -> null
            else -> computeLocalEllipticWeight(q0, q1, q2, q)
        }
    }
    val w = data.map { (xi, yi) -> acos(yi.divOrDefault(xi) { 1.0 }.coerceIn(-1.0, 1.0)) * 2 }
            .average()
            .let { cos(it / 2) }
            .coerceIn(-1.0, 1.0)
    //val xx = data.sumByDouble { (x, _) -> x * x }
    //val xy = data.sumByDouble { (x, y) -> x * y }

    return w//xy.divOrDefault(xx) { 0.999999 }.coerceIn(-0.999999, 0.999999)
}

fun computeLocalEllipticWeight(p0: Point, p1: Point, p2: Point, p: Point): Pair<Double, Double> {
    val a = p1.projectTo(line(p, p + (p2 - p0)).orThrow())
    val b = p1.projectTo(Line(p0, p2))
    val t = (a - p1).dot(b - p1).tryDiv(b.distSquare(p1)).orThrow()//.value().orDefault(0.0)
    val x = p1.lerp(t, p0.middle(p2))
    val dd = x.distSquare(p)
    val ll = p0.distSquare(p2) / 4
    val yi = dd + t * t * ll - 2 * t * ll
    val xi = ll * t * t - dd
    //if (yi.divOrDefault(xi) { 1.0 }.coerceIn(-1.0, 1.0) in listOf(-1.0, 1.0))
    //  println("%2.3f / %2.3f = %2.3f".format(yi, xi, yi / xi))
    return 1.0 to yi.divOrDefault(xi) { 1.0 }.coerceIn(-1.0, 1.0)//xi to yi
}

