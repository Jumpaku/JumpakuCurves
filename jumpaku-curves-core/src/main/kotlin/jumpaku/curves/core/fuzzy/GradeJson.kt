package jumpaku.curves.core.fuzzy

import com.github.salomonbrys.kotson.double
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import jumpaku.commons.json.JsonConverterBase

object GradeJson : JsonConverterBase<Grade>() {

    override fun toJson(src: Grade): JsonPrimitive = src.run { JsonPrimitive(value) }

    override fun fromJson(json: JsonElement): Grade = Grade(json.double)
}

fun Grade.toJson(): JsonPrimitive = GradeJson.toJson(this)

val JsonElement.grade: Grade get() = GradeJson.fromJson(this)