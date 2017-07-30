package org.jumpaku.core.curve

import org.jumpaku.core.affine.Affine


interface Transformable : Curve {

    fun transform(a: Affine): Curve
}
