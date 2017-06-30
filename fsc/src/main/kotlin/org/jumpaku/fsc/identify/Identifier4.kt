package org.jumpaku.fsc.identify

import io.vavr.API.*
import org.jumpaku.core.curve.rationalbezier.RationalBezier
import org.jumpaku.fsc.identify.reference.Circular
import org.jumpaku.fsc.identify.reference.Elliptic
import org.jumpaku.fsc.identify.reference.Linear


class Identifier4 : IdentifierWithRule({ fsc ->
    val linear = Linear.create(fsc.domain.begin, fsc.domain.end, fsc)
    val circular = Circular.create(fsc.domain.begin, fsc.domain.end, fsc)
    val elliptic = Elliptic.create(fsc.domain.begin, fsc.domain.end, fsc)

    val pL = linear.isValidFor(fsc)
    val pC = circular.isValidFor(fsc)
    val pE = elliptic.isValidFor(fsc)

    Map(
            Tuple(CurveClass.LineSegment,   Tuple( pL                , Array(linear.lineSegment.asCrispRationalBezier))),
            Tuple(CurveClass.CircularArc,   Tuple(!pL and  pC        , Array(circular.conicSection.asCrispRationalBezier))),
            Tuple(CurveClass.EllipticArc,   Tuple(!pL and !pC and  pE, Array(elliptic.conicSection.asCrispRationalBezier))),
            Tuple(CurveClass.OpenFreeCurve, Tuple(!pL and !pC and !pE, fsc.toBeziers().map { RationalBezier.fromBezier(it) }))
    )
})