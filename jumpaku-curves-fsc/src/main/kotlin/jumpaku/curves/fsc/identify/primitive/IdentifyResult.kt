package jumpaku.curves.fsc.identify.primitive

import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.identify.primitive.reference.Reference
import kotlin.collections.component1
import kotlin.collections.component2

class IdentifyResult(
        grades: Map<CurveClass, Grade>,
        val linear: Reference,
        val circular: Reference,
        val elliptic: Reference) {

    val grades: Map<CurveClass, Grade> = grades.toMap()

    val grade: Grade get() = grades.maxBy { it.value }!!.value

    val curveClass: CurveClass get() = grades.maxBy { (_, m) -> m }!!.key

    init {
        require(grades.isNotEmpty()) { "empty grades" }
    }
}

