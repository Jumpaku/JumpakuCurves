package jumpaku.fsc.test.snap.conicsection

import jumpaku.core.util.component1
import jumpaku.core.util.component2
import jumpaku.core.geom.Point
import jumpaku.core.geom.Vector
import jumpaku.core.transform.Rotate
import jumpaku.core.curve.rationalbezier.ConicSection
import jumpaku.core.json.parseJson
import jumpaku.fsc.identify.CurveClass
import jumpaku.fsc.identify.reference.reparametrize
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.conicsection.ConicSectionSnapResult
import jumpaku.fsc.snap.conicsection.ConicSectionSnapper
import jumpaku.fsc.snap.conicsection.ConjugateCombinator
import jumpaku.fsc.snap.point.MFGS
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldEqualTo
import org.junit.Test

class ConicSectionSnapperTest {

    val urlString = "/jumpaku/fsc/test/snap/conicsection/"
    fun resourceText(name: String): String = javaClass.getResource(urlString + name).readText()

    val w = 1280.0
    val h = 720.0

    val baseGrid = Grid(
            spacing = 64.0,
            magnification = 2,
            origin = Point.xy(w/2, h/2),
            rotation = Rotate(Vector.K, 0.0),
            fuzziness = 16.0)

    val conicSectionSnapper = ConicSectionSnapper(
            MFGS(
                    baseGrid = baseGrid,
                    minResolution = -5,
                    maxResolution = 5),
            ConjugateCombinator())

    @Test
    fun testSnap_L() {
        println("Snap_L")
        for (i in 0..0) {
            val cs = resourceText("ConicSectionL$i.json").parseJson().flatMap { ConicSection.fromJson(it) }.get()
            val e = resourceText("SnapResultL$i.json").parseJson().flatMap { ConicSectionSnapResult.fromJson(it) }.get()
            val a = conicSectionSnapper.snap(cs, CurveClass.LineSegment) { candidate ->
                reparametrize(cs).isPossible(jumpaku.fsc.identify.reference.reparametrize(candidate.snappedConicSection), 15)
            }
            a.candidate.featurePoints.size().shouldEqualTo(e.candidate.featurePoints.size())
            a.candidate.featurePoints.zip(e.candidate.featurePoints).forEach { (a, e) ->
                a.snapped.get().gridPoint.shouldEqual(e.snapped.get().gridPoint)
            }
        }
    }

    @Test
    fun testSnap_CA() {
        println("Snap_CA")
        for (i in 0..2) {
            val cs = resourceText("ConicSectionCA$i.json").parseJson().flatMap { ConicSection.fromJson(it) }.get()
            val e = resourceText("SnapResultCA$i.json").parseJson().flatMap { ConicSectionSnapResult.fromJson(it) }.get()
            val a = conicSectionSnapper.snap(cs, CurveClass.CircularArc) { candidate ->
                reparametrize(cs).isPossible(jumpaku.fsc.identify.reference.reparametrize(candidate.snappedConicSection), 15)
            }
            a.candidate.featurePoints.size().shouldEqualTo(e.candidate.featurePoints.size())
            a.candidate.featurePoints.zip(e.candidate.featurePoints).forEach { (a, e) ->
                a.snapped.get().gridPoint.shouldEqual(e.snapped.get().gridPoint)
            }
        }
    }

    @Test
    fun testSnap_EA() {
        println("Snap_EA")
        for (i in 0..2) {
            val cs = resourceText("ConicSectionEA$i.json").parseJson().flatMap { ConicSection.fromJson(it) }.get()
            val e = resourceText("SnapResultEA$i.json").parseJson().flatMap { ConicSectionSnapResult.fromJson(it) }.get()
            val a = conicSectionSnapper.snap(cs, CurveClass.EllipticArc) { candidate ->
                reparametrize(cs).isPossible(jumpaku.fsc.identify.reference.reparametrize(candidate.snappedConicSection), 15)
            }
            a.candidate.featurePoints.size().shouldEqualTo(e.candidate.featurePoints.size())
            a.candidate.featurePoints.zip(e.candidate.featurePoints).forEach { (a, e) ->
                a.snapped.get().gridPoint.shouldEqual(e.snapped.get().gridPoint)
            }
        }
    }
}