package jumpaku.curves.core.transform

import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase

object SimilarityJson : JsonConverterBase<Similarity>() {

    override fun fromJson(json: JsonElement): Similarity = Similarity(TransformJson.fromJson(json).matrix)

    override fun toJson(src: Similarity): JsonElement = TransformJson.toJson(src)
}