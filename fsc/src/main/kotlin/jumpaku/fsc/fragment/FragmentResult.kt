package jumpaku.fsc.fragment

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.collection.Array
import jumpaku.core.json.ToJson

data class FragmentResult(val fragments: Array<Fragment>): ToJson {

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("fragments" to jsonArray(fragments.map { it.toJson() }))
}

val JsonElement.fragmentResult: FragmentResult get() = FragmentResult(
        Array.ofAll(this["fragments"].array.map { it.fragment }))