package jumpaku.curves.fsc.test.snap.conicsection

import com.github.salomonbrys.kotson.get
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.json.parseJson
import jumpaku.curves.core.test.curve.rationalbezier.shouldEqualToConicSection
import jumpaku.curves.fsc.identify.primitive.CurveClass
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.conicsection.ConicSectionSnapper
import jumpaku.curves.fsc.snap.conicsection.ConjugateCombinator
import jumpaku.curves.fsc.snap.point.MFGS
import org.junit.Test

class ConicSectionSnapperTest {

    val urlString = "/jumpaku/curves/fsc/test/snap/conicsection/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val w = 1280.0
    val h = 720.0

    val grid = Grid(
            baseSpacing = 64.0,
            magnification = 2,
            origin = Point.xy(w/2, h/2),
            rotation = Rotate(Vector.K, 0.0),
            baseFuzziness = 16.0)

    val conicSectionSnapper = ConicSectionSnapper(
            MFGS(minResolution = -5, maxResolution = 5),
            ConjugateCombinator())

    @Test
    fun testSnap_L() {
        println("Snap_L")
        for (i in 0..0) {
            val cs = resourceText("ConicSectionL$i.json").parseJson().tryMap { ConicSection.fromJson(it) }.orThrow()
            val e = resourceText("SnapResultL$i.json").parseJson().tryMap { ConicSection.fromJson(it["snappedConicSection"]["value"]) }.orThrow()
            val a = conicSectionSnapper.snap(grid, cs, CurveClass.LineSegment, evaluator = ConicSectionSnapper.evaluateWithReference(cs))
            a.snappedConicSection.orThrow().shouldEqualToConicSection(e)
        }
    }

    @Test
    fun testSnap_CA() {
        println("Snap_CA")
        for (i in 0..2) {
            val cs = resourceText("ConicSectionCA$i.json").parseJson().tryMap { ConicSection.fromJson(it) }.orThrow()
            val e = resourceText("SnapResultCA$i.json").parseJson().tryMap { ConicSection.fromJson(it["snappedConicSection"]["value"]) }.orThrow()
            val a = conicSectionSnapper.snap(grid, cs, CurveClass.CircularArc, evaluator = ConicSectionSnapper.evaluateWithReference(cs))
            a.snappedConicSection.orThrow().shouldEqualToConicSection(e)
        }
    }

    @Test
    fun testSnap_EA() {
        println("Snap_EA")
        for (i in 0..2) {
            val cs = resourceText("ConicSectionEA$i.json").parseJson().tryMap { ConicSection.fromJson(it) }.orThrow()
            val e = resourceText("SnapResultEA$i.json").parseJson().tryMap { ConicSection.fromJson(it["snappedConicSection"]["value"]) }.orThrow()
            val a = conicSectionSnapper.snap(grid, cs, CurveClass.EllipticArc, evaluator = ConicSectionSnapper.evaluateWithReference(cs))
            a.snappedConicSection.orThrow().shouldEqualToConicSection(e)
        }
    }
}