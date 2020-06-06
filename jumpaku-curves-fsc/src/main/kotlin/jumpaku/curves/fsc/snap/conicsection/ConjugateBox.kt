package jumpaku.curves.fsc.snap.conicsection

import jumpaku.commons.math.tryDiv
import jumpaku.curves.core.curve.bezier.ConicSection
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.transform.Calibrate
import jumpaku.curves.core.transform.Transform
import org.apache.commons.math3.util.FastMath


class ConjugateBox(val transform: Transform) {

    private val r2 = FastMath.sqrt(2.0)

    val top: Point = transform(Point.xy(0.0, r2))

    val bottom: Point = transform(Point.xy(0.0, -r2))

    val left: Point = transform(Point.xy(-r2, 0.0))

    val right: Point = transform(Point.xy(r2, 0.0))

    val center = transform(Point.origin)

    val topLeft: Point = transform(Point.xy(-1.0, 1.0))

    val topRight: Point = transform(Point.xy(1.0, 1.0))

    val bottomLeft: Point = transform(Point.xy(-1.0, -1.0))

    val bottomRight: Point = transform(Point.xy(1.0, -1.0))

    companion object {

        fun ofConicSection(conicSection: ConicSection): ConjugateBox {
            fun transform(deepConicSection: ConicSection): Transform {
                val w = deepConicSection.weight
                val t = (1 + w).tryDiv(1 - w).value().map { ((1 - FastMath.sqrt(it)) / 2).coerceIn(0.0, 0.5) }
                return Calibrate(Point.xy(0.0, 1.0) to deepConicSection.far,
                        Point.xy(-1.0, 0.0) to deepConicSection(t.orThrow()),
                        Point.xy(1.0, 0.0) to deepConicSection(1 - t.orThrow()))
            }

            val transform = when {
                conicSection.center().isDefined -> transform(
                        if (conicSection.weight <= conicSection.complement().weight) conicSection
                        else conicSection.complement())
                else -> {
                    Calibrate(Point.xy(0.0, 1.0) to conicSection.far,
                            Point.xy(-1.0, 0.0) to conicSection.begin,
                            Point.xy(1.0, 0.0) to conicSection.end)
                }
            }
            return ConjugateBox(transform)
        }
    }
}
