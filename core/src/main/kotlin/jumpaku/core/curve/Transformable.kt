package jumpaku.core.curve

import jumpaku.core.affine.Affine


interface Transformable : Curve {

    fun transform(a: Affine): Curve
}
