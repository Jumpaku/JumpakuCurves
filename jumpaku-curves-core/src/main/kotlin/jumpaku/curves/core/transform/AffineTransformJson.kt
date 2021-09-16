package jumpaku.curves.core.transform

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.jsonArray
import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase
import org.apache.commons.math3.linear.MatrixUtils

object AffineTransformJson : JsonConverterBase<AffineTransform>() {

    override fun toJson(src: AffineTransform): JsonElement = src.run {
        jsonArray(matrix.data.map { jsonArray(it.asList()) })
    }

    override fun fromJson(json: JsonElement): AffineTransform =
            json.array.map { it.array.map { it.double }.toDoubleArray() }.toTypedArray()
                    .let { AffineTransform.ofMatrix(MatrixUtils.createRealMatrix(it)) }
}