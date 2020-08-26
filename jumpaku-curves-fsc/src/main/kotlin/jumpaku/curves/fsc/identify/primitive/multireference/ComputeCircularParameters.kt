package jumpaku.curves.fsc.identify.primitive.multireference

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.geom.lerp
import jumpaku.curves.core.geom.line
import jumpaku.curves.core.geom.middle
import org.apache.commons.math3.analysis.solvers.BrentSolver
import kotlin.math.acos
import kotlin.math.cos

fun <C : Curve> computeGlobalCircularParameters(fsc: ReparametrizedCurve<C>, generations: Int, nSamples: Int = 100, nDivisions: Int = 10): Pair<Double, List<Double>> {
    val original = fsc.originalCurve
    val fscReparametrizer = fsc.reparametrizer
    val divisions = fsc.sample(nDivisions)
            .map { it.run { copy(param = fscReparametrizer.toOriginal(param)) } }
    val samples = original.sample(nSamples)
    val begin = samples.first()
    val end = samples.last()
    val frontPoints = divisions
            .map { p -> computeLocalCircularPartition(fsc, begin.param, p.param) }
    val backPoints = divisions
            .map { p -> computeLocalCircularPartition(fsc, p.param, end.param) }
    val middlePoints = divisions.indices
            .map { i -> computeLocalCircularFar(fsc, frontPoints[i].param, backPoints[i].param) }
    val halfWeights = divisions.indices
            .map { i -> computeLocalCircularWeight(frontPoints[i], middlePoints[i], backPoints[i]) }
    println(halfWeights)
    val globalHalfWeight = halfWeights.map { acos(it) * 2 }.average().let { cos(it / 2) }
    val nFars = (1 shl (generations + 2)) + 1
    val farParams = fsc.domain.sample(nFars).map { fscReparametrizer.toOriginal(it) }
    return globalHalfWeight to farParams
}

fun <C : Curve> computeLocalCircularPartition(fsc: ReparametrizedCurve<C>, tFront: Double, tBack: Double): ParamPoint {
    val original = fsc.originalCurve
    val reparametrizer = fsc.reparametrizer
    val sFront = reparametrizer.toArcLengthRatio(tFront)
    val sBack = reparametrizer.toArcLengthRatio(tBack)
    val sPartition = sFront.middle(sBack)
    val tPartition  = reparametrizer.toOriginal(sPartition)
    return ParamPoint(original(tPartition), tPartition)
}

fun <C : Curve> computeLocalCircularFar(fsc: ReparametrizedCurve<C>, tFront: Double, tBack: Double): ParamPoint {
    val original = fsc.originalCurve
    val front = original(tFront)
    val back = original(tBack)
    val t = BrentSolver(1.0e-6).solve(50, {
        val c = original(tFront.lerp(it, tBack))
        val l = front.distSquare(c)
        val r = back.distSquare(c)
        l - r
    }, 0.0, 1.0)
    val tFar = tFront.lerp(t, tBack)
    return ParamPoint(original(tFar), tFar)
}

fun computeLocalCircularWeight(p0: ParamPoint, p1: ParamPoint, p2: ParamPoint): Double {
    val hh = line(p0.point, p2.point)
            .tryMap { p1.point.distSquare(it) }
            .orRecover { p1.point.distSquare(p1.middle(p2).point) }
    val ll = p0.point.distSquare(p2.point) / 4
    return ((ll - hh) / (ll + hh)).coerceIn(-1.0, 1.0)
}
