package jumpaku.curves.core.transform

interface SimilarityTransformable<T : SimilarityTransformable<T>> {

    /**
     * Applies similarity transformation that scales fuzziness.
     */
    fun similarityTransform(a: SimilarityTransform): T
}