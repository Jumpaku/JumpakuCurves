package org.jumpaku.core.fitting

import io.vavr.collection.Array
import org.jumpaku.core.curve.Curve
import org.jumpaku.core.curve.ParamPoint


interface Fitting<out C : Curve> {

    fun fit(data: Array<ParamPoint>): C
}