package jumpaku.curves.fsc.identify.primitive.multireference

import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve


interface ReferenceElementBuilder<C : Curve> {

    val elementsSize: Int

    val globalWeight: Double

    fun build(): Map<Int, ReferenceElement>
}


interface MultiReferenceGenerator {

    val generations: Int

    fun <C : Curve> generate(fsc: ReparametrizedCurve<C>): MultiReference
}

abstract class AbstractRecursiveReferenceGenerator(override val generations: Int) : MultiReferenceGenerator {

    abstract fun <C : Curve> createElementBuilder(fsc: ReparametrizedCurve<C>): ReferenceElementBuilder<C>

    override fun <C : Curve> generate(fsc: ReparametrizedCurve<C>): MultiReference {
        val builder = createElementBuilder(fsc)
        val elements = builder
                .build()
                .run { Array(size) { i -> getValue(i) } }
                .toList()
        val w = builder.globalWeight
        val domain = Interval(-1 / (2 * w + 2), (2 * w + 3) / (2 * w + 2))
        return RecursiveMultiReference(generations, domain, elements)
    }
}
