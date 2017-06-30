package org.jumpaku.fsc.identify

import io.vavr.API
import io.vavr.Tuple2
import io.vavr.collection.Map
import io.vavr.collection.Set
import io.vavr.collection.Array
import org.jumpaku.core.curve.rationalbezier.RationalBezier
import org.jumpaku.core.fuzzy.Grade


class Result(val resultTable: Map<CurveClass, Tuple2<Grade, Array<RationalBezier>>>){

    private fun sortByGrade(): Array<Tuple2<CurveClass, Tuple2<Grade, Array<RationalBezier>>>> {
        return resultTable.toArray().sortBy { it._2()._1() } .reverse()
    }

    val curveClass: CurveClass =  sortByGrade().head()._1()

    val rationalBezier: Array<RationalBezier> = sortByGrade().head()._2()._2()

    val grade: Grade = sortByGrade().head()._2()._1()

    val curveClasses: Set<CurveClass> = resultTable.keySet()

    val rationalBeziers: Map<CurveClass, Array<RationalBezier>> = resultTable.map { k, v -> API.Tuple(k, v._2()) }

    val grades: Map<CurveClass, Grade> = resultTable.map { k, v -> API.Tuple(k, v._1()) }
}