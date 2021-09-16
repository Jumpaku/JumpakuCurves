package jumpaku.curves.core.transform

interface AffineTransformable<T : AffineTransformable<T>> {

    /**
     * Applies affine transformation that ignores fuzziness.
     */
    fun affineTransform(a: AffineTransform): T
}

