package jumpaku.core.json

import com.github.salomonbrys.kotson.toJson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.vavr.control.Option
import io.vavr.control.Try
import java.io.File
import java.net.URI
import java.net.URL
import java.nio.file.Path

val prettyGson = GsonBuilder().setPrettyPrinting().serializeNulls().create()!!

interface ToJson {
    fun toJson(): JsonElement
    fun toJsonString(): String = prettyGson.toJson(toJson())
}

fun String.parseJson(): Option<JsonElement> = Try.ofSupplier { JsonParser().parse(this) }.toOption()

fun File.parseJson(): Option<JsonElement> = readText().parseJson()

fun Path.parseJson(): Option<JsonElement> = toFile().parseJson()

fun URL.parseJson(): Option<JsonElement> = readText().parseJson()
