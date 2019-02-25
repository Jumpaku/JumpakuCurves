package jumpaku.curves.core.json

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import jumpaku.curves.core.util.Result
import jumpaku.curves.core.util.result
import java.io.File
import java.net.URL
import java.nio.file.Path

val prettyGson = GsonBuilder().setPrettyPrinting().serializeNulls().create()!!

interface ToJson {
    fun toJson(): JsonElement
    fun toJsonString(): String = prettyGson.toJson(toJson())
}

fun String.parseJson(): Result<JsonElement> = result { JsonParser().parse(this) }

fun File.parseJson(): Result<JsonElement> = readText().parseJson()

fun Path.parseJson(): Result<JsonElement> = toFile().parseJson()

fun URL.parseJson(): Result<JsonElement> = readText().parseJson()
