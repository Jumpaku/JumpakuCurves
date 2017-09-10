package jumpaku.core.json

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.vavr.control.Try

val prettyGson = GsonBuilder().setPrettyPrinting().create()!!

fun String.parseToJson(): Try<JsonElement> = Try.ofSupplier { JsonParser().parse(this) }
