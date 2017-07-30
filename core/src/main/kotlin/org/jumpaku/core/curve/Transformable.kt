package org.jumpaku.core.curve

import org.jumpaku.core.affine.Transform


interface Transformable : Curve {

    fun transform(a: Transform): Curve
}
