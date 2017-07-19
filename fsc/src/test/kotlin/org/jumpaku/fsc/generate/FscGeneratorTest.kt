package org.jumpaku.fsc.generate

import com.github.salomonbrys.kotson.fromJson
import io.vavr.collection.Array
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.withPrecision
import org.jumpaku.core.affine.Point
import org.jumpaku.core.affine.pointAssertThat
import org.jumpaku.core.curve.ParamPointJson
import org.jumpaku.core.curve.bspline.BSpline
import org.jumpaku.core.curve.bspline.BSplineJson
import org.jumpaku.core.curve.bspline.bSplineAssertThat
import org.jumpaku.core.curve.fuzzyCurveAssertThat
import org.jumpaku.core.json.prettyGson
import org.jumpaku.core.util.component1
import org.jumpaku.core.util.component2
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths


class FscGeneratorTest {

    val path: Path = Paths.get("./src/test/resources/org/jumpaku/fsc/generate/")

    @Test
    fun testGenerate() {
        println("Generate")
        for (i in 0..5) {
            val dataFile = path.resolve("FscGenerationData$i.json").toFile()
            val dataJson = prettyGson.fromJson<kotlin.Array<ParamPointJson>>(dataFile.readText())
            val data = Array.ofAll(dataJson.map { it.paramPoint() })
            val a = FscGenerator(3, 0.1).generate(data)
            val e = prettyGson.fromJson<BSplineJson>(path.resolve("FscGenerationFsc$i.json").toFile().readText()).bSpline()

            fuzzyCurveAssertThat(a).isEqualToFuzzyCurve(e, 12.0, 30)

        }
    }
}
