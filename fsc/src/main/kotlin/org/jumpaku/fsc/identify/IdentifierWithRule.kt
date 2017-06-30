package org.jumpaku.fsc.identify

import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.Map
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.rationalbezier.RationalBezier
import org.jumpaku.core.fuzzy.Grade


open class IdentifierWithRule(val rule: (BSpline) -> Map<CurveClass, Tuple2<Grade, Array<RationalBezier>>>) : Identifier {

    override fun identify(fsc: BSpline): Result = Result(rule(fsc))
}