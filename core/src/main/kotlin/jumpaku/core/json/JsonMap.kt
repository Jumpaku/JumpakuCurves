package jumpaku.core.json

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import io.vavr.Tuple2
import io.vavr.collection.HashMap
import io.vavr.collection.Map
import jumpaku.core.util.component1
import jumpaku.core.util.component2


fun <K : JsonElement, V: JsonElement, M : Map<K, V>>jsonMap(map: M): JsonArray = jsonArray(map.toArray().map { (key, value) ->
    jsonObject("key" to key, "value" to value) })

val JsonElement.hashMap: Map<JsonElement, JsonElement> get() = HashMap.ofEntries(array.map { Tuple2(it["key"], it["value"]) })


