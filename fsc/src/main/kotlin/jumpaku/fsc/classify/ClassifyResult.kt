package jumpaku.fsc.classify

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import io.vavr.Tuple2
import io.vavr.collection.Map
import io.vavr.collection.Set
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.fuzzy.Grade
import jumpaku.core.json.ToJson
import jumpaku.core.json.hashMap
import jumpaku.core.json.jsonMap
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.hashMap


class ClassifyResult(val grades: Map<CurveClass, Grade>): ToJson {

    constructor(vararg pairs: Pair<CurveClass, Grade>) : this(hashMap(*pairs))

    init {
        require(grades.nonEmpty()) { "empty grades" }
    }

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject(
            "grades" to jsonMap(grades.map { k, v -> Tuple2(k.name.toJson(), v.toJson()) }))

    val curveClass: CurveClass = grades.maxBy { (_, m) -> m } .map { it._1() }.get()

    val grade: Grade = grades.values().max().get()

    val curveClasses: Set<CurveClass> = grades.keySet()

    companion object {

        fun fromJson(json: JsonElement): Option<ClassifyResult> = Try.ofSupplier {
            ClassifyResult(json["grades"].hashMap.map { c, g ->
                    Tuple2(CurveClass.valueOf(c.string), Grade.fromJson(g.asJsonPrimitive).get()) })
        }.toOption()
    }
}
