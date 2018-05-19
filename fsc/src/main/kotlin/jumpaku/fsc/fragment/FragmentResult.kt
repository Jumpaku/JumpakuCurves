package jumpaku.fsc.fragment

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.collection.Array
import io.vavr.control.Option
import io.vavr.control.Try
import jumpaku.core.json.ToJson

data class FragmentResult(val fragments: Array<Fragment>): ToJson {

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("fragments" to jsonArray(fragments.map { it.toJson() }))

    companion object {

        fun fromJson(json: JsonElement): Option<FragmentResult> = Try.ofSupplier {
            FragmentResult(Array.ofAll(json["fragments"].array.flatMap { Fragment.fromJson(it) }))
        }.toOption()
    }
}
