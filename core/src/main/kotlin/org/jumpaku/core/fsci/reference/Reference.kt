package org.jumpaku.core.fsci.reference

import org.apache.commons.math3.analysis.solvers.BrentSolver
import org.apache.commons.math3.util.FastMath
import org.jumpaku.core.affine.Point
import org.jumpaku.core.curve.FuzzyCurve
import org.jumpaku.core.curve.Interval
import org.jumpaku.core.curve.arclength.ArcLengthAdapter
import org.jumpaku.core.curve.rationalbezier.ConicSection
import org.jumpaku.core.fuzzy.Grade


interface Reference {

    val fuzzyCurve: FuzzyCurve

    fun validate(fuzzyCurve: FuzzyCurve): Grade = fuzzyCurve.isPossible(this.fuzzyCurve)
}


internal fun evaluateWithoutDomain(t: Double, conicSection: ConicSection): Point {
    val rem = t - 2 * FastMath.floor(t / 2)
    return when {
        rem <= 1 -> conicSection(rem)
        else -> conicSection.complement()(2 - rem)
    }
}

/**
 * absolute arc-length from beginning point of circular.
 */
internal fun conicSectionArcLengthWithoutDomain(t: Double, circular: ArcLengthAdapter, complement: ArcLengthAdapter): Double {
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

internal fun createDomain(t0: Double, t1: Double, fscArcLength: ArcLengthAdapter, conicSection: ConicSection): Interval {
    val l0 = fscArcLength.arcLengthUntil(t0)
    val l1 = fscArcLength.arcLengthUntil(t1)
    val l = fscArcLength.arcLength()

    val arcLengthCircular = conicSection.toArcLengthCurve()
    val arcLengthComplement = conicSection.complement().toArcLengthCurve()

    val relative = 1.0e-8
    val absolute = 1.0e-5
    val targetFrontLength = l0 / (l1 - l0) * arcLengthCircular.arcLength()
    var a = 0.0
    while (conicSectionArcLengthWithoutDomain(a, arcLengthCircular, arcLengthComplement) <= targetFrontLength){
        a -= 1
    }
    val begin = BrentSolver(relative, absolute).solve(50, {
        val la = conicSectionArcLengthWithoutDomain(it, arcLengthCircular, arcLengthComplement)
        la - targetFrontLength
    }, a, a + 1, a + 0.5)

    val targetBackLength = (l - l0)/(l1 - l0) * arcLengthCircular.arcLength()
    var b = 1.0
    while (conicSectionArcLengthWithoutDomain(b, arcLengthCircular, arcLengthComplement) <= targetBackLength){
        b += 1
    }
    val end = BrentSolver(relative, absolute).solve(50, {
        val lb = conicSectionArcLengthWithoutDomain(it, arcLengthCircular, arcLengthComplement)
        lb - targetBackLength
    }, b - 1, b, b - 0.5)

    return Interval(begin, end)
}
