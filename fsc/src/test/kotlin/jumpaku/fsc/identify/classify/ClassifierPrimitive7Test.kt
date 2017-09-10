package jumpaku.fsc.identify.classify

import com.github.salomonbrys.kotson.fromJson
import io.vavr.collection.Array
import jumpaku.core.curve.bspline.bSpline
import jumpaku.core.curve.fuzzyCurveAssertThat
import jumpaku.core.json.parseToJson
import jumpaku.core.json.prettyGson
import org.assertj.core.api.Assertions.*
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

class ClassifierPrimitive7Test {

    val path: Path = Paths.get("./src/test/resources/jumpaku/fsc/identify/classify/")

    @Test
    fun testClassify() {
        println("ClassifierPrimitive7.Classify")
        val c = ClassifierPrimitive7()
        (1..4).forEach { i ->
            val fscFile = path.resolve("LineSegmentFsc$i.json").toFile()
            val fsc = fscFile.readText().parseToJson().get().bSpline
            assertThat(c.classify(fsc).curveClass).`as`(fscFile.path).isEqualTo(CurveClass.LineSegment)
        }
        (1..4).forEach { i ->
            val fscFile = path.resolve("CircleFsc$i.json").toFile()
            val fsc = fscFile.readText().parseToJson().get().bSpline
            assertThat(c.classify(fsc).curveClass).`as`(fscFile.path).isEqualTo(CurveClass.Circle)
        }
        (1..4).forEach { i ->
            val fscFile = path.resolve("CircularArcFsc$i.json").toFile()
            val fsc = fscFile.readText().parseToJson().get().bSpline
            assertThat(c.classify(fsc).curveClass).`as`(fscFile.path).isEqualTo(CurveClass.CircularArc)
        }
        (1..4).forEach { i ->
            val fscFile = path.resolve("EllipseFsc$i.json").toFile()
            val fsc = fscFile.readText().parseToJson().get().bSpline
            assertThat(c.classify(fsc).curveClass).`as`(fscFile.path).isEqualTo(CurveClass.Ellipse)
        }
        (1..4).forEach { i ->
            val fscFile = path.resolve("EllipticArcFsc$i.json").toFile()
            val fsc = fscFile.readText().parseToJson().get().bSpline
            assertThat(c.classify(fsc).curveClass).`as`(fscFile.path).isEqualTo(CurveClass.EllipticArc)
        }
        (1..4).forEach { i ->
            val fscFile = path.resolve("ClosedFreeCurveFsc$i.json").toFile()
            val fsc = fscFile.readText().parseToJson().get().bSpline
            assertThat(c.classify(fsc).curveClass).`as`(fscFile.path).isEqualTo(CurveClass.ClosedFreeCurve)
        }
        (1..4).forEach { i ->
            val fscFile = path.resolve("OpenFreeCurveFsc$i.json").toFile()
            val fsc = fscFile.readText().parseToJson().get().bSpline
            assertThat(c.classify(fsc).curveClass).`as`(fscFile.path).isEqualTo(CurveClass.OpenFreeCurve)
        }
    }

}