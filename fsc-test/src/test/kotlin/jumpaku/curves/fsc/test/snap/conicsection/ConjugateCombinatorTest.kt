package jumpaku.fsc.test.snap.conicsection

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.test.geom.shouldEqualToPoint
import jumpaku.fsc.snap.conicsection.CircularFeaturePoints
import jumpaku.fsc.snap.conicsection.ConjugateCombinator
import jumpaku.fsc.snap.conicsection.EllipticFeaturePoints
import jumpaku.fsc.snap.conicsection.LinearFeaturePoints
import org.amshove.kluent.shouldEqualTo
import org.apache.commons.math3.util.FastMath
import org.junit.Test

class ConjugateCombinatorTest {

    val r2 = FastMath.sqrt(2.0)

    val l = ConicSection.lineSegment(Point.xy(1.0, 1.0), Point.xy(-1.0, -1.0))

    val c0 = ConicSection(Point.xy(-r2 / 2, r2 / 2), Point.xy(0.0, 1.0), Point.xy(r2 / 2, r2 / 2), r2 / 2)

    val c1 = ConicSection(Point.xy(-1.0, 0.0), Point.xy(0.0, 1.0), Point.xy(1.0, 0.0), 0.0)

    val c2 = ConicSection(Point.xy(-r2 / 2, -r2 / 2), Point.xy(0.0, 1.0), Point.xy(r2 / 2, -r2 / 2), -r2 / 2)

    val e0 = ConicSection(Point.xy(-r2, r2 / 2), Point.xy(0.0, 1.0), Point.xy(r2, r2 / 2), r2 / 2)

    val e1 = ConicSection(Point.xy(-2.0, 0.0), Point.xy(0.0, 1.0), Point.xy(2.0, 0.0), 0.0)

    val e2 = ConicSection(Point.xy(-r2, -r2 / 2), Point.xy(0.0, 1.0), Point.xy(r2, -r2 / 2), -r2 / 2)

    val conjugateCombinator = ConjugateCombinator()

    @Test
    fun testLinearCombinations() {
        println("LinearCombinations")
        val aopen = conjugateCombinator.linearCombinations(l, true)
        val eopen = listOf(LinearFeaturePoints(Point.xy(1.0, 1.0), Point.xy(-1.0, -1.0)))
        aopen.size.shouldEqualTo(eopen.size)
        for ((a, e) in aopen.zip(eopen)) {
            val (a0, a1) = a
            val (e0, e1) = e
            a0.shouldEqualToPoint(e0)
            a1.shouldEqualToPoint(e1)
        }
        val aclosed = conjugateCombinator.linearCombinations(l, false)
        val eclosed = listOf(LinearFeaturePoints(Point.xy(0.0, 0.0), Point.xy(0.0, 0.0)))
        aclosed.size.shouldEqualTo(eclosed.size)
        for ((a, e) in aclosed.zip(eclosed)) {
            val (a0, a1) = a
            val (e0, e1) = e
            a0.shouldEqualToPoint(e0)
            a1.shouldEqualToPoint(e1)
        }
    }

    @Test
    fun testCircularCombinations() {
        println("CircularCombinations")
        val aopen0 = conjugateCombinator.circularCombinations(c0, true)
        val eopen0 = listOf(
                CircularFeaturePoints(Point.xy(-r2 / 2, r2 / 2), Point.xy(r2 / 2, r2 / 2), Point.xy(0.0, 1.0)),
                CircularFeaturePoints(Point.xy(-r2 / 2, r2 / 2),Point.xy(r2 / 2, r2 / 2), Point.xy(0.0, r2))
        )
        aopen0.size.shouldEqualTo(eopen0.size)
        for ((a, e) in aopen0.zip(eopen0)) {
            val (a0, a1, a2) = a
            val (e0, e1, e2) = e
            a0.shouldEqualToPoint(e0)
            a1.shouldEqualToPoint(e1)
            a2.shouldEqualToPoint(e2)
        }

        val aopen1 = conjugateCombinator.circularCombinations(c1, true)
        val eopen1 = listOf(
                CircularFeaturePoints(Point.xy(-1.0, 0.0), Point.xy(1.0, 0.0), Point.xy(0.0, 1.0)),
                CircularFeaturePoints(Point.xy(-1.0, 0.0), Point.xy(1.0, 0.0), Point.xy(0.0, 1.0)),
                CircularFeaturePoints(Point.xy(-r2, 0.0), Point.xy(r2, 0.0), Point.xy(0.0, 1.0))
        )
        aopen1.size.shouldEqualTo(eopen1.size)
        for ((a, e) in aopen1.zip(eopen1)) {
            val (a0, a1, a2) = a
            val (e0, e1, e2) = e
            a0.shouldEqualToPoint(e0)
            a1.shouldEqualToPoint(e1)
            a2.shouldEqualToPoint(e2)
        }

        val aopen2 = conjugateCombinator.circularCombinations(c2, true)
        val eopen2 = listOf(
                CircularFeaturePoints(Point.xy(-r2 / 2, -r2 / 2), Point.xy(r2 / 2, -r2 / 2), Point.xy(0.0, 1.0)),
                CircularFeaturePoints(Point.xy(-1.0, 0.0), Point.xy(1.0, 0.0), Point.xy(0.0, 1.0)),
                CircularFeaturePoints(Point.xy(-r2, 0.0), Point.xy(r2, 0.0), Point.xy(0.0, 1.0))
        )
        aopen2.size.shouldEqualTo(eopen2.size)
        for ((a, e) in aopen2.zip(eopen2)) {
            val (a0, a1, a2) = a
            val (e0, e1, e2) = e
            a0.shouldEqualToPoint(e0)
            a1.shouldEqualToPoint(e1)
            a2.shouldEqualToPoint(e2)
        }

        val aclosed3 = conjugateCombinator.circularCombinations(c2, false)
        val eclosed3 = listOf(
                CircularFeaturePoints(Point.xy(-1.0, 0.0), Point.xy(1.0, 0.0), Point.xy(0.0, 1.0)),
                CircularFeaturePoints(Point.xy(-1.0, 0.0), Point.xy(1.0, 0.0), Point.xy(0.0, -1.0)),
                CircularFeaturePoints(Point.xy(0.0, 1.0), Point.xy(0.0, -1.0), Point.xy(-1.0, 0.0)),
                CircularFeaturePoints(Point.xy(0.0, 1.0), Point.xy(0.0, -1.0), Point.xy(1.0, 0.0)),

                CircularFeaturePoints(Point.xy(-r2, 0.0), Point.xy(r2, 0.0), Point.xy(0.0, 1.0)),
                CircularFeaturePoints(Point.xy(-r2, 0.0), Point.xy(r2, 0.0), Point.xy(0.0, -1.0)),
                CircularFeaturePoints(Point.xy(0.0, r2), Point.xy(0.0, -r2), Point.xy(-1.0, 0.0)),
                CircularFeaturePoints(Point.xy(0.0, r2), Point.xy(0.0, -r2), Point.xy(1.0, 0.0))
        )
        aclosed3.size.shouldEqualTo(eclosed3.size)
        for ((a, e) in aclosed3.zip(eclosed3)) {
            val (a0, a1, a2) = a
            val (e0, e1, e2) = e
            a0.shouldEqualToPoint(e0)
            a1.shouldEqualToPoint(e1)
            a2.shouldEqualToPoint(e2)
        }
    }

    @Test
    fun testEllipticCombinations() {
        println("EllipticCombinations")
        val aopen0 = conjugateCombinator.ellipticCombinations(e0, true)
        val eopen0 = listOf(
                EllipticFeaturePoints(Point.xy(-r2, r2 / 2), Point.xy(r2, r2 / 2), Point.xy(0.0, 1.0)),
                EllipticFeaturePoints(Point.xy(-r2, r2 / 2), Point.xy(r2, r2 / 2), Point.xy(0.0, r2))
        )
        aopen0.size.shouldEqualTo(eopen0.size)
        for ((a, e) in aopen0.zip(eopen0)) {
            val (a0, a1, a2) = a
            val (e0, e1, e2) = e
            a0.shouldEqualToPoint(e0)
            a1.shouldEqualToPoint(e1)
            a2.shouldEqualToPoint(e2)
        }

        val aopen1 = conjugateCombinator.ellipticCombinations(e1, true)
        val eopen1 = listOf(
                EllipticFeaturePoints(Point.xy(-2.0, 0.0), Point.xy(2.0, 0.0), Point.xy(0.0, 1.0)),
                EllipticFeaturePoints(Point.xy(-2.0, 0.0), Point.xy(2.0, 0.0), Point.xy(0.0, r2)),
                EllipticFeaturePoints(Point.xy(-2.0, 0.0), Point.xy(0.0, 1.0), Point.xy(2.0, 0.0)),
                EllipticFeaturePoints(Point.xy(-2 * r2, 0.0), Point.xy(0.0, r2), Point.xy(2 * r2, 0.0))
        )
        aopen1.size.shouldEqualTo(eopen1.size)
        for ((a, e) in aopen1.zip(eopen1)) {
            val (a0, a1, a2) = a
            val (e0, e1, e2) = e
            a0.shouldEqualToPoint(e0)
            a1.shouldEqualToPoint(e1)
            a2.shouldEqualToPoint(e2)
        }

        val aopen2 = conjugateCombinator.ellipticCombinations(e2, true)
        val eopen2 = listOf(
                EllipticFeaturePoints(Point.xy(-r2, -r2 / 2), Point.xy(r2, -r2 / 2), Point.xy(0.0, 1.0)),
                EllipticFeaturePoints(Point.xy(-r2, -r2 / 2), Point.xy(r2, -r2 / 2), Point.xy(0.0, r2)),
                EllipticFeaturePoints(Point.xy(-2.0, 0.0), Point.xy(0.0, 1.0), Point.xy(2.0, 0.0)),
                EllipticFeaturePoints(Point.xy(-2 * r2, 0.0), Point.xy(0.0, r2), Point.xy(2 * r2, 0.0))
        )
        aopen2.size.shouldEqualTo(eopen2.size)
        for ((a, e) in aopen2.zip(eopen2)) {
            val (a0, a1, a2) = a
            val (e0, e1, e2) = e
            a0.shouldEqualToPoint(e0)
            a1.shouldEqualToPoint(e1)
            a2.shouldEqualToPoint(e2)
        }

        val aclosed3 = conjugateCombinator.ellipticCombinations(e2, false)
        val eclosed3 = listOf(
                EllipticFeaturePoints(Point.xy(-2.0, 0.0), Point.xy(0.0, 1.0), Point.xy(2.0, 0.0)),
                EllipticFeaturePoints(Point.xy(0.0, 1.0), Point.xy(2.0, 0.0), Point.xy(0.0, -1.0)),
                EllipticFeaturePoints(Point.xy(2.0, 0.0), Point.xy(0.0, -1.0), Point.xy(-2.0, 0.0)),
                EllipticFeaturePoints(Point.xy(0.0, -1.0), Point.xy(-2.0, 0.0), Point.xy(0.0, 1.0)),

                EllipticFeaturePoints(Point.xy(-2 * r2, 0.0), Point.xy(0.0, r2), Point.xy(2 * r2, 0.0)),
                EllipticFeaturePoints(Point.xy(0.0, r2), Point.xy(2 * r2, 0.0), Point.xy(0.0, -r2)),
                EllipticFeaturePoints(Point.xy(2 * r2, 0.0), Point.xy(0.0, -r2), Point.xy(-2 * r2, 0.0)),
                EllipticFeaturePoints(Point.xy(0.0, -r2), Point.xy(-2 * r2, 0.0), Point.xy(0.0, r2))
        )
        aclosed3.size.shouldEqualTo(eclosed3.size)
        for ((a, e) in aclosed3.zip(eclosed3)) {
            val (a0, a1, a2) = a
            val (e0, e1, e2) = e
            a0.shouldEqualToPoint(e0)
            a1.shouldEqualToPoint(e1)
            a2.shouldEqualToPoint(e2)
        }
    }
}