package jumpaku.fsc.test.classify.reference

import com.github.salomonbrys.kotson.array
import io.vavr.collection.Array
import jumpaku.core.affine.Point
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.core.test.affine.pointAssertThat
import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.fsc.classify.reference.CircularReferenceGenerator
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths


class CircularTest {

    val path: Path = Paths.get("./src/test/resources/jumpaku/fsc/test/classify/reference")

    @Test
    fun testReference() {
        println("Reference")
        val s = path.resolve("circularFsc.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
        val eps = Array.ofAll(path.resolve("circularReference.json").parseJson().get().array.flatMap { Point.fromJson(it) })
        val rps = CircularReferenceGenerator().generateScattered(s, nSamples = 99).evaluateAll(eps.size())
        rps.zip(eps).forEach { (r, e) -> pointAssertThat(r).isEqualToPoint(e) }
    }
}