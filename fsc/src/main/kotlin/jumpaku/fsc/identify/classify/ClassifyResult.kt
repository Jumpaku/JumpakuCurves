package jumpaku.fsc.identify.classify

import io.vavr.collection.HashMap
import io.vavr.collection.Map
import io.vavr.collection.Set
import jumpaku.core.fuzzy.Grade
import jumpaku.core.json.KeyValue
import jumpaku.core.json.hashMapValue
import jumpaku.core.json.jsonValue
import jumpaku.core.json.prettyGson
import jumpaku.core.util.component1
import jumpaku.core.util.component2


data class ClassifyResult(val grades: Map<CurveClass, Grade>){

    constructor(vararg pairs: Pair<CurveClass, Grade>) : this(HashMap.ofAll(mutableMapOf(*pairs)))

    init {
        require(grades.nonEmpty()) { "empty grades" }
    }

    fun json(): ClassifyResultJson = ClassifyResultJson(this)

    override fun toString(): String = prettyGson.toJson(json())

    val curveClass: CurveClass = grades.maxBy { (_, m) -> m } .map { it._1() }.get()

    val grade: Grade = grades.values().max().get()

    val curveClasses: Set<CurveClass> = grades.keySet()
}


data class ClassifyResultJson(val grades: List<KeyValue<CurveClass, Double>>) {

    constructor(classifyResult: ClassifyResult): this(classifyResult.grades.jsonValue { it.value })

    fun classifyResult(): ClassifyResult = ClassifyResult(grades.hashMapValue { Grade(it) })
}