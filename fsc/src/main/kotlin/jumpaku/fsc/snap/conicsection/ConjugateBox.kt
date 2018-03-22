package jumpaku.fsc.snap.conicsection

import io.vavr.Tuple3
import jumpaku.core.affine.*
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.clamp
import jumpaku.core.util.divOption
import org.apache.commons.math3.util.FastMath


class ConjugateBox(val transform: Affine) {

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
            fun transform(deepConicSection: ConicSection): Affine {
                val w = deepConicSection.weight
                val t = (1 + w).divOption(1 - w).map { clamp((1 - FastMath.sqrt(it)) / 2, 0.0, 0.5) }
                val p0 = deepConicSection.far
                val p1 = deepConicSection(t.get())
                val p2 = deepConicSection(1 - t.get())
                return calibrateToPlane(Tuple3(Point.xy(0.0, 1.0), Point.xy(-1.0, 0.0), Point.xy(1.0, 0.0)), Tuple3(p0, p1, p2))
                        .get()
            }
            val transform = when{
                conicSection.center().isDefined -> transform(
                        if (conicSection.weight <= conicSection.complement().weight) conicSection
                        else conicSection.complement())
                else -> {
                    val p0 = conicSection.far
                    val p1 = conicSection.begin
                    val p2 = conicSection.end
                    calibrateToPlane(
                            Tuple3(Point.xy(0.0, 1.0), Point.xy(-1.0, 0.0), Point.xy(1.0, 0.0)), Tuple3(p0, p1, p2)).get()
                }
            }
            return ConjugateBox(transform)
        }
    }
}
