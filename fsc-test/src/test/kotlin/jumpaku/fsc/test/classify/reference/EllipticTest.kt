package jumpaku.fsc.test.classify.reference

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.json.parseJson
import jumpaku.core.test.curve.polyline.polylineAssertThat
import jumpaku.core.test.curve.rationalbezier.conicSectionAssertThat
import jumpaku.fsc.classify.reference.Elliptic
import jumpaku.fsc.classify.reference.EllipticGenerator
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths


class EllipticTest {

    val path: Path = Paths.get("./src/test/resources/jumpaku/fsc/test/classify/reference")
    
    val generator = EllipticGenerator(nSamples = 25)

    @Test
    fun testEllipticGenerate() {
        println("EllipticGenerate")
        val s = path.resolve("ellipticFsc.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
        val e = path.resolve("ellipticPolyline.json").parseJson().flatMap { Polyline.fromJson(it) }.get()
        val a = generator.generate(s, t0 = s.domain.begin, t1 = s.domain.end).polyline
        polylineAssertThat(a).isEqualToPolyline(e)
    }

    @Test
    fun testEllipticConicSection() {
        println("EllipticConicSection")
        val s = path.resolve("ellipticFsc.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
        val e = path.resolve("ellipticPolyline.json").parseJson().flatMap { Polyline.fromJson(it) }
                .map { Elliptic(it, generator.nSamples).conicSection }.get()
        val a = generator.generate(s, t0 = s.domain.begin, t1 = s.domain.end).conicSection
        conicSectionAssertThat(a).isEqualConicSection(e)
    }
}