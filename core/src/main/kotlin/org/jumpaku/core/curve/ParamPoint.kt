package org.jumpaku.core.curve

import org.jumpaku.core.affine.Divisible
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.PointJson
import org.jumpaku.core.affine.divide
import org.jumpaku.core.json.prettyGson

data class ParamPoint(val point: Point, val param: Double) : Divisible<ParamPoint> {

    override fun divide(t: Double, p: ParamPoint): ParamPoint {
        return ParamPoint(point.divide(t, p.point), param.divide(t, p.param))
    }

    override fun toString(): String = prettyGson.toJson(json())

    fun json(): ParamPointJson = ParamPointJson(this)

    companion object{

        fun now(point: Point): ParamPoint = ParamPoint(point, System.nanoTime()*1.0e-9)
    }
}

data class ParamPointJson(
        val point: PointJson,
        val param: Double){

    constructor(paramPoint: ParamPoint) : this(paramPoint.point.json(), paramPoint.param)

    fun paramPoint(): ParamPoint = ParamPoint(point.point(), param)
}