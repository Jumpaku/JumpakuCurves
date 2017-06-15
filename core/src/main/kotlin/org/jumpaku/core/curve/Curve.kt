package org.jumpaku.core.curve

import io.vavr.collection.Array
import org.jumpaku.core.affine.Point

/**

 * @author Jumpaku
 */
interface Curve : Function1<Double, Point> {

    val domain: Interval

    override fun invoke(t: Double): Point {
        require(t in domain) { "t=$t is out of $domain" }

        return evaluate(t)
    }

    /**
     * @param t
     * @return
     * @throws IllegalArgumentException t !in domain
     */
    fun evaluate(t: Double): Point

    fun evaluateAll(n: Int): Array<Point> = domain.sample(n).map(this)

    fun evaluateAll(delta: Double): Array<Point> = domain.sample(delta).map(this)
}