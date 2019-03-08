package jumpaku.curves.core.json

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonArray
import com.google.gson.JsonElement


fun <K : JsonElement, V: JsonElement, M : Map<K, V>> jsonMap(map: M): JsonArray =
        jsonArray(map.map { (key, value) -> jsonObject("key" to key, "value" to value) })

val JsonElement.map: Map<JsonElement, JsonElement> get() = array.map { Pair(it["key"], it["value"]) }.toMap()


