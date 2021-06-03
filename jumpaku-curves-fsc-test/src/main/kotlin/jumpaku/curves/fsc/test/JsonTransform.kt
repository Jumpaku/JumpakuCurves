package jumpaku.curves.fsc.test

import com.google.gson.JsonElement
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.bspline_old.BSplineJson
import java.nio.file.Paths

fun transformBSpline(json: JsonElement): JsonElement {
    val b1 = BSplineJson.fromJson(json)
    val b2 = BSpline(
        b1.controlPoints,
        jumpaku.curves.core.curve.bspline.KnotVector.of(b1.degree, b1.knotVector.extractedKnots)
    )
    return jumpaku.curves.core.curve.bspline.BSplineJson.toJson(b2)
}

fun main() {
    println(Paths.get("./").toAbsolutePath())
    val base = Paths.get("./jumpaku-curves-fsc-test/src/test/resources/jumpaku/curves/fsc/test/")
    for (n in 0..12) {
        val p1 = base.resolve("identify/nquarter/FscElliptic$n.json")
        println(p1.toFile().exists())
        val j1 = p1.toFile().readText().parseJson()
        val j2 = j1.let { transformBSpline(it) }
        p1.toFile().writeText(j2.toString())
    }

}