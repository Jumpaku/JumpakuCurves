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
import jumpaku.fsc.classify.reference.Circular
import jumpaku.fsc.classify.reference.CircularGenerator
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths


class CircularTest {

    val path: Path = Paths.get("./src/test/resources/jumpaku/fsc/test/classify/reference")

    val generator = CircularGenerator(nSamples = 25)

    @Test
    fun testCircularGenerate() {
        println("CircularGenerate")
        val s = path.resolve("circularFsc.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
        val e = path.resolve("circularPolyline.json").parseJson().flatMap { Polyline.fromJson(it) }.get()
        val a = generator.generate(s, t0 = s.domain.begin, t1 = s.domain.end).polyline
        polylineAssertThat(a).isEqualToPolyline(e)
    }

    @Test
    fun testCircularConicSection() {
        println("CircularConicSection")
        val s = path.resolve("circularFsc.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
        val e = path.resolve("circularPolyline.json").parseJson().flatMap { Polyline.fromJson(it) }
                .map { Circular(it).conicSection }.get()
        val a = generator.generate(s, t0 = s.domain.begin, t1 = s.domain.end).conicSection
        conicSectionAssertThat(a).isEqualConicSection(e)
    }
}