package jumpaku.fsc.classify.reference

import io.vavr.API
import org.apache.commons.math3.analysis.solvers.BrentSolver
import org.apache.commons.math3.util.FastMath
import jumpaku.core.affine.Point
import jumpaku.core.curve.Interval
import jumpaku.core.curve.arclength.ArcLengthAdapter
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.fuzzy.Grade
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.divOption


interface Reference {

    fun isValidFor(fsc: BSpline): Grade
}

fun evaluateWithoutDomain(t: Double, conicSection: ConicSection): Point {
    val rem = t - 2 * FastMath.floor(t / 2)
    return when {
        rem <= 1 -> conicSection(rem)
        else -> conicSection.complement()(2 - rem)
    }
}

/**
 * absolute arc-length from beginning point of a conicSection.
 */
fun conicSectionArcLengthWithoutDomain(t: Double, circular: ArcLengthAdapter, complement: ArcLengthAdapter): Double {
    val n = FastMath.floor(FastMath.abs(t) / 2)
    val circumference = circular.arcLength() + complement.arcLength()
    val rem = t - 2 * FastMath.floor(t / 2)
    return circumference * n + when {
        t >= 0 && rem <= 1 -> circular.arcLengthUntil(rem)
        t >= 0 && rem > 1 -> complement.arcLengthUntil(rem - 1) + circular.arcLength()
        t < 0 && rem <= 1 -> circular.arcLengthUntil(1 - rem) + complement.arcLength()
        t < 0 && rem > 1 -> complement.arcLengthUntil(2 - rem)
        else -> error("")
    }
}

/**
 *
 */
fun createDomain(t0: Double, t1: Double, fscArcLength: ArcLengthAdapter, conicSection: ConicSection): Interval {
    val l0 = fscArcLength.arcLengthUntil(t0)
    val l1 = fscArcLength.arcLengthUntil(t1)
    if(1.0.divOption(l1 - l0).isEmpty){
        return Interval.ZERO_ONE
    }

    val l = fscArcLength.arcLength()

    val arcLengthConic = conicSection.toArcLengthCurve()
    val arcLengthComplement = conicSection.complement().toArcLengthCurve()

    val relative = 1.0e-8
    val absolute = 1.0e-5
    val targetFrontLength = l0 / (l1 - l0) * arcLengthConic.arcLength()
    var a = 0.0
    while (conicSectionArcLengthWithoutDomain(a, arcLengthConic, arcLengthComplement) <= targetFrontLength){
        a -= 1
    }
    val begin = BrentSolver(relative, absolute).solve(50, {
        val la = conicSectionArcLengthWithoutDomain(it, arcLengthConic, arcLengthComplement)
        la - targetFrontLength
    }, a, 0.0, a + 0.5)

    val targetBackLength = (l - l0)/(l1 - l0) * arcLengthConic.arcLength()
    var b = 1.0
    while (conicSectionArcLengthWithoutDomain(b, arcLengthConic, arcLengthComplement) <= targetBackLength){
        b += 1
    }
    val end = BrentSolver(relative, absolute).solve(50, {
        val lb = conicSectionArcLengthWithoutDomain(it, arcLengthConic, arcLengthComplement)
        lb - targetBackLength
    }, 1.0, b, b - 0.5)

    return Interval(begin, end)
}


fun mostFarPointOnFsc(p: Double, fsc: BSpline): Double {
    return fsc.domain.sample(100)
            .map { API.Tuple(it, fsc(p).distSquare(fsc(it))) }
            .maxBy { (_, a) -> a }
            .map { it._1() } .get()
}
