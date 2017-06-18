package org.jumpaku.core.curve

import org.jumpaku.core.affine.Transform


interface CrispTransformable : Curve {

    fun crispTransform(a: Transform): Curve
}
