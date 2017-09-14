package jumpaku.core.json

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.vavr.Tuple2
import io.vavr.collection.HashMap
import io.vavr.collection.Map
import io.vavr.control.Option
import jumpaku.core.util.component1
import jumpaku.core.util.component2


fun <E : JsonElement>jsonOption(opt: Option<E>): JsonObject = jsonObject(
        "value" to if(opt.isDefined) opt.get() else jsonNull)
val JsonElement.option: Option<JsonElement> get() = Option
        .`when`(!obj["value"].isJsonNull) { obj["value"] }

fun <K : JsonElement, V: JsonElement, M : Map<K, V>>jsonMap(map: M): JsonArray = jsonArray(map.toArray().map { (key, value) ->
    jsonObject("key" to key, "value" to value) })
val JsonElement.hashMap: Map<JsonElement, JsonElement> get() = HashMap
        .ofEntries(array.map { Tuple2(it["key"], it["value"]) })


