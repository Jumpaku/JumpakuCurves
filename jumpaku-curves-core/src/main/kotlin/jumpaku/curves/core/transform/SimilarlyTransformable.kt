package jumpaku.curves.core.transform

interface SimilarlyTransformable<T : SimilarlyTransformable<T>> {

    /**
     * Applies similarity transformation that scales fuzziness.
     */
    fun similarlyTransform(a: SimilarityTransform): T
}