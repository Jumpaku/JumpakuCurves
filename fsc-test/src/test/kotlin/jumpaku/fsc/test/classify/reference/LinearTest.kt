package jumpaku.fsc.test.classify.reference

import com.github.salomonbrys.kotson.array
import io.vavr.collection.Array
import jumpaku.core.affine.Point
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.json.parseJson
import jumpaku.core.test.affine.pointAssertThat
import jumpaku.core.test.curve.polyline.polylineAssertThat
import jumpaku.core.test.curve.rationalbezier.conicSectionAssertThat
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.classify.reference.Linear
import jumpaku.fsc.classify.reference.LinearGenerator
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths


class LinearTest {

    val path: Path = Paths.get("./src/test/resources/jumpaku/fsc/test/classify/reference")
    val generator = LinearGenerator(nSamples = 25)

    @Test
    fun testLinearGenerate() {
        println("LinearGenerate")
        val s = path.resolve("linearFsc.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
        val e = path.resolve("linearPolyline.json").parseJson().flatMap { Polyline.fromJson(it) }.get()
        val a = generator.generate(s, t0 = s.domain.begin, t1 = s.domain.end).polyline
        assertThat(a.isPossible(e, 15).value).isGreaterThan(0.9)
    }

    @Test
    fun testLinearConicSection() {
        println("LinearConicSection")
        val s = path.resolve("linearFsc.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
        val e = path.resolve("linearPolyline.json").parseJson().flatMap { Polyline.fromJson(it) }
                .map { Linear(it).conicSection }.get()
        val a = generator.generate(s, t0 = s.domain.begin, t1 = s.domain.end).conicSection
        assertThat(a.isPossible(e, 15).value).isGreaterThan(0.9)
    }
}