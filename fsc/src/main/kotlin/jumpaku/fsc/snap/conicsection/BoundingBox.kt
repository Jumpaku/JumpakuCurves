package jumpaku.fsc.snap.conicsection

import io.vavr.Tuple4
import io.vavr.collection.Stream
import io.vavr.control.Option
import jumpaku.core.affine.Affine
import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.core.affine.calibrate
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import jumpaku.fsc.classify.CurveClass
import jumpaku.fsc.snap.conicsection.candidate.featurePoints
import jumpaku.fsc.snap.conicsection.candidate.unitCircularArc
import org.apache.commons.math3.util.FastMath



fun boundingBox(conicSection: ConicSection, type: BoundingBox.Type): Option<BoundingBox> {
    val cs = if (conicSection.weight >= 0.0) conicSection.complement() else conicSection
    val csFeatures = cs.featurePoints(CurveClass.Ellipse)
    val unit = unitCircularArc(cs.weight)
    val unitFeatures = unit.featurePoints(CurveClass.Circle)
    val features = when (type) {
        BoundingBox.Type.Square -> Stream.of(FeatureType.Diameter0, FeatureType.Diameter1, FeatureType.Diameter2)
        BoundingBox.Type.Diamond -> Stream.of(FeatureType.Extra0, FeatureType.Extra1, FeatureType.Extra2)
    }

    val (u0, u1, u2) = features.map { unitFeatures[it].get() }
    val u3 = u0 + u0.normal(u1, u2).getOrElse(Vector())
    val (c0, c1, c2) = features.map { csFeatures[it].get() }
    val c3 = c0 + c0.normal(c1, c2).getOrElse(Vector())

    return calibrate(Tuple4(u0, u1, u2, u3), Tuple4(c0, c1, c2, c3)).map {
        unitBoundingBox(type).transform(it)
    }
}

fun unitBoundingBox(type: BoundingBox.Type): BoundingBox {
    val r2 = FastMath.sqrt(2.0)
    return when (type) {
        BoundingBox.Type.Square -> BoundingBox(
                topLeft = Point.xy(-1.0, 1.0),
                topCenter = Point.xy(0.0, 1.0),
                topRight = Point.xy(1.0, 1.0),
                middleLeft = Point.xy(-1.0, 0.0),
                middleCenter = Point.xy(0.0, 0.0),
                middleRight = Point.xy(1.0, 0.0),
                bottomLeft = Point.xy(-1.0, -1.0),
                bottomCenter = Point.xy(0.0, -1.0),
                bottomRight = Point.xy(1.0, -1.0))
        BoundingBox.Type.Diamond -> BoundingBox(
                topLeft = Point.xy(-r2, 0.0),
                topCenter = Point.xy(-r2 / 2, r2 / 2),
                topRight = Point.xy(0.0, r2),
                middleLeft = Point.xy(-r2 / 2, -r2 / 2),
                middleCenter = Point.xy(0.0, 0.0),
                middleRight = Point.xy(r2 / 2, r2 / 2),
                bottomLeft = Point.xy(0.0, -r2),
                bottomCenter = Point.xy(r2 / 2, 0.0),
                bottomRight = Point.xy(r2, 0.0))
    }
}

data class BoundingBox(
        val topLeft: Point,
        val topCenter: Point,
        val topRight: Point,
        val middleLeft: Point,
        val middleCenter: Point,
        val middleRight: Point,
        val bottomLeft: Point,
        val bottomCenter: Point,
        val bottomRight: Point) {

    fun transform(transform: Affine): BoundingBox = BoundingBox(
            topLeft = transform(topLeft),
            topCenter = transform(topCenter),
            topRight = transform(topRight),
            middleLeft = transform(middleLeft),
            middleCenter = transform(middleCenter),
            middleRight = transform(middleRight),
            bottomLeft = transform(bottomLeft),
            bottomCenter = transform(bottomCenter),
            bottomRight = transform(bottomRight))

    enum class Type {
        Square, Diamond
    }
}