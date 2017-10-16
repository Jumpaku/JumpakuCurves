package jumpaku.fsc.classify

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import io.vavr.Tuple2
import io.vavr.collection.HashMap
import io.vavr.collection.Map
import io.vavr.collection.Set
import jumpaku.core.fuzzy.Grade
import jumpaku.core.fuzzy.grade
import jumpaku.core.json.ToJson
import jumpaku.core.json.hashMap
import jumpaku.core.json.jsonMap
import jumpaku.core.util.component1
import jumpaku.core.util.component2


class ClassifyResult(val grades: Map<CurveClass, Grade>): ToJson {

    constructor(vararg pairs: Pair<CurveClass, Grade>) : this(HashMap.ofAll(mutableMapOf(*pairs)))

    init {
        require(grades.nonEmpty()) { "empty grades" }
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "grades" to jsonMap(grades.map { k, v -> Tuple2(k.name.toJson(), v.toJson()) }))

    operator fun component1(): CurveClass = curveClass

    operator fun component2(): Grade = grade

    val curveClass: CurveClass = grades.maxBy { (_, m) -> m } .map { it._1() }.get()

    val grade: Grade = grades.values().max().get()

    val curveClasses: Set<CurveClass> = grades.keySet()
}

val JsonElement.classifyResult: ClassifyResult get() = ClassifyResult(
        this["grades"].hashMap.map { c, g -> Tuple2(CurveClass.valueOf(c.string), g.grade) })
