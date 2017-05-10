package org.jumpaku.curve

import org.jumpaku.affine.Point

/**

 * @author Jumpaku
 */
interface Curve : Function1<Double, Point> {
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
    fun evaluate(t: Double?): Point

    val domain: Interval
}