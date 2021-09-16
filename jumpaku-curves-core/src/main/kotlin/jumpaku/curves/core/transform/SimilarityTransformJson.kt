package jumpaku.curves.core.transform

import com.google.gson.JsonElement
import jumpaku.commons.json.JsonConverterBase

object SimilarityTransformJson : JsonConverterBase<SimilarityTransform>() {

    override fun fromJson(json: JsonElement): SimilarityTransform = SimilarityTransform(AffineTransformJson.fromJson(json))

    override fun toJson(src: SimilarityTransform): JsonElement = AffineTransformJson.toJson(src.asAffine())
}