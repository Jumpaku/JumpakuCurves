package org.jumpaku.core.curve

import org.jumpaku.core.affine.Point

/**

 * @author Jumpaku
 */
interface Curve : Function1<Double, Point> {

    val domain: Interval

    override fun invoke(t: Double): Point {
        if (t !in domain) {
            throw IllegalArgumentException("t=$t is out of $domain")
        }

        return evaluate(t)
    }

    /**
     * @param t
     * @return
     * @throws IllegalArgumentException t !in domain
     */
    fun evaluate(t: Double): Point
}