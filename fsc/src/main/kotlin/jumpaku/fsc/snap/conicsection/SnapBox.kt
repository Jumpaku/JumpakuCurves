package jumpaku.fsc.snap.conicsection

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import io.vavr.Tuple4
import io.vavr.collection.Array
import io.vavr.collection.Map
import io.vavr.collection.Stream
import io.vavr.control.Option
import jumpaku.core.affine.*
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.json.ToJson
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.util.component3
import jumpaku.core.util.hashMap
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
        val a = when(type) {
            BoundingBox.Type.Square -> it
            BoundingBox.Type.Diamond -> identity
                    .andRotate(unitZ(), FastMath.PI/4)
                    .andScale(FastMath.sqrt(2.0))
                    .andThen(it)
        }
        BoundingBox(a)
    }
}


class BoundingBox(val a: Affine = identity) : ToJson {

    operator fun get(row: Row, column: Column): Point = boundingBox[row].flatMap { it[column] }.map(a).get()

    private val boundingBox: Map<Row, Map<Column, Point>> = hashMap(
            Row.Top to hashMap(
                    Column.Left to Point.xy(-1.0, 1.0),
                    Column.Center to Point.xy(0.0, 1.0),
                    Column.Right to Point.xy(1.0, 1.0)
            ),
            Row.Middle to hashMap(
                    Column.Left to Point.xy(-1.0, 0.0),
                    Column.Center to Point.xy(0.0, 0.0),
                    Column.Right to Point.xy(1.0, 0.0)
            ),
            Row.Bottom to hashMap(
                    Column.Left to Point.xy(-1.0, -1.0),
                    Column.Center to Point.xy(0.0, -1.0),
                    Column.Right to Point.xy(1.0, -1.0))
    )

    override fun toString(): String = toJsonString()

    override fun toJson(): JsonElement = jsonObject("transform" to a.toJson())

    enum class Type {
        Square, Diamond
    }

    enum class Column {
        Left, Center, Right
    }

    enum class Row {
        Top, Middle, Bottom
    }
}

fun JsonElement.boundingBox(): BoundingBox = BoundingBox(this["transform"].affine)
