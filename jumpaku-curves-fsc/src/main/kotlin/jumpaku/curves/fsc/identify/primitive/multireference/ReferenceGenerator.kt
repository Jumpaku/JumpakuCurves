package jumpaku.curves.fsc.identify.primitive.multireference

import jumpaku.commons.math.divOrDefault
import jumpaku.commons.math.isEven
import jumpaku.commons.math.isOdd
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.geom.Point
import kotlin.math.sqrt


interface ReferenceElementBuilder<C : Curve> {

    val elementsSize: Int

    val globalWeight: Double

    fun build(): List<ReferenceElement>
}


abstract class AbstractReferenceGenerator(val generations: Int) {

    abstract fun <C : Curve> createElementBuilder(fsc: ReparametrizedCurve<C>): ReferenceElementBuilder<C>

    fun <C : Curve> generate(fsc: ReparametrizedCurve<C>): MultiReference {
        val builder = createElementBuilder(fsc)
        val elements = builder.build()
        val w = builder.globalWeight
        val domain = Interval(-1 / (2 * w + 2), (2 * w + 3) / (2 * w + 2))
        return MultiReference(generations, domain, elements)
    }
}
