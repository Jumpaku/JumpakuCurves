package org.jumpaku.fsc.identify.classify

import io.vavr.collection.HashMap
import io.vavr.collection.Map
import io.vavr.collection.Set
import org.jumpaku.core.fuzzy.Grade
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2


class Result(val grades: Map<CurveClass, Grade>){

    constructor(vararg pairs: Pair<CurveClass, Grade>) : this(HashMap.ofAll(mutableMapOf(*pairs)))

    init {
        require(grades.nonEmpty()) { "classification result is empty" }
    }

    val curveClass: CurveClass = grades.maxBy { (_, m) -> m } .map { it._1() }.get()

    val grade: Grade = grades.values().max().get()

    val curveClasses: Set<CurveClass> = grades.keySet()
}