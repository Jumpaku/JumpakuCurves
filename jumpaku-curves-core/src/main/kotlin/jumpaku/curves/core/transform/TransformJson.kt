package jumpaku.curves.core.transform

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.jsonArray
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import org.apache.commons.math3.linear.MatrixUtils

object TransformJson : JsonConverterBase<Transform>() {

    override fun toJson(src: Transform): JsonElement = src.run {
        jsonArray(matrix.data.map { jsonArray(it.asList()) })
    }

    override fun fromJson(json: JsonElement): Transform =
            json.array.map { it.array.map { it.double }.toDoubleArray() }.toTypedArray()
                    .let { Transform.ofMatrix(MatrixUtils.createRealMatrix(it)) }
}