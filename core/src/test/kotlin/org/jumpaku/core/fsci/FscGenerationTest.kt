package org.jumpaku.core.fsci

import com.github.salomonbrys.kotson.fromJson
import io.vavr.collection.Array
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.withPrecision
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.TimeSeriesPointJson
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.bspline.BSplineJson
import org.jumpaku.core.curve.bspline.bSplineAssertThat
import org.jumpaku.core.json.prettyGson
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2
import org.junit.Test
import java.nio.file.Paths


class FscGenerationTest {

    val path = Paths.get("./src/test/resources/org/jumpaku/core/fsci/")

    @Test
    fun testGenerate() {
        println("Generate")
        for (i in 0..4) {
            val dataFile = path.resolve("FscGenerationData$i.json").toFile()
            val dataJson = prettyGson.fromJson<kotlin.Array<TimeSeriesPointJson>>(dataFile.readText())
            val data = Array.ofAll(dataJson.map { it.timeSeriesPoint() })
            val a = FscGeneration(3, 0.1).generate(data)
            val e = prettyGson.fromJson<BSplineJson>(path.resolve("FscGenerationFsc$i.json").toFile().readText()).bSpline()
            bSplineAssertThat(BSpline(a.controlPoints.map(Point::toCrisp), a.knots))
                    .isEqualToBSpline(BSpline(e.controlPoints.map(Point::toCrisp), e.knots), 0.1)
            a.controlPoints.map(Point::r).zip(e.controlPoints.map(Point::r))
                    .forEach { (a, e) ->
                        assertThat(a).isEqualTo(e, withPrecision(10.0))
                    }
        }
    }
}
