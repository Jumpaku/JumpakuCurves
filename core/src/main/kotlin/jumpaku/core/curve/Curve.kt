package jumpaku.core.curve

import io.vavr.Function1
import io.vavr.collection.Array
import jumpaku.core.affine.Point

/**

 * @author Jumpaku
 */
interface Curve : Function1<Double, Point> {

    val domain: Interval

    operator fun invoke(t: Double): Point = apply(t)

    override fun apply(t: Double): Point {
        require(t in domain) { "t=$t is out of $domain" }

        return evaluate(t)
    }

    /**
     * @param t
     * @return
     * @throws IllegalArgumentException t !in domain
     */
    fun evaluate(t: Double): Point

    fun evaluateAll(n: Int): Array<Point> = domain.sample(n).map(this::evaluate)

    fun evaluateAll(delta: Double): Array<Point> = domain.sample(delta).map(this::evaluate)

    fun sample(n: Int): Array<ParamPoint> = domain.sample(n).map { ParamPoint(this(it), it) }

    fun sample(delta: Double): Array<ParamPoint> = domain.sample(delta).map { ParamPoint(this(it), it) }
}