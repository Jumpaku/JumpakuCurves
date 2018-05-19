package jumpaku.fsc.test.classify.reference

import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.curve.polyline.Polyline
import jumpaku.core.json.parseJson
import jumpaku.fsc.classify.reference.Circular
import jumpaku.fsc.classify.reference.CircularGenerator
import org.amshove.kluent.shouldBeGreaterThan
import org.junit.Test

class CircularTest {

    val urlString = "/jumpaku/fsc/test/classify/reference/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val generator = CircularGenerator(nSamples = 25)

    @Test
    fun testCircularGenerate() {
        println("CircularGenerate")
        val s = resourceText("circularFsc.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
        val e = resourceText("circularPolyline.json").parseJson().flatMap { Polyline.fromJson(it) }.get()
        val a = generator.generate(s, t0 = s.domain.begin, t1 = s.domain.end).polyline
        a.isPossible(e, 15).value.shouldBeGreaterThan(0.9)
    }

    @Test
    fun testCircularConicSection() {
        println("CircularConicSection")
        val s = resourceText("circularFsc.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
        val e = resourceText("circularPolyline.json").parseJson().flatMap { Polyline.fromJson(it) }
                .map { Circular(it).conicSection }.get()
        val a = generator.generate(s, t0 = s.domain.begin, t1 = s.domain.end).conicSection
        a.isPossible(e, 15).value.shouldBeGreaterThan(0.9)
    }
}