package jumpaku.fsc.identify.classify

import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import io.vavr.Tuple2
import io.vavr.collection.HashMap
import io.vavr.collection.Map
import io.vavr.collection.Set
import jumpaku.core.fuzzy.Grade
import jumpaku.core.json.jsonMap
import jumpaku.core.util.component1
import jumpaku.core.util.component2


data class ClassifyResult(val grades: Map<CurveClass, Grade>){

    constructor(vararg pairs: Pair<CurveClass, Grade>) : this(HashMap.ofAll(mutableMapOf(*pairs)))

    init {
        require(grades.nonEmpty()) { "empty grades" }
    }

    override fun toString(): String = toJson().toString()

    fun toJson(): JsonElement = jsonMap(grades.map { k, v -> Tuple2(k.name.toJson(), v.toJson()) })

    val curveClass: CurveClass = grades.maxBy { (_, m) -> m } .map { it._1() }.get()

    val grade: Grade = grades.values().max().get()

    val curveClasses: Set<CurveClass> = grades.keySet()
}
