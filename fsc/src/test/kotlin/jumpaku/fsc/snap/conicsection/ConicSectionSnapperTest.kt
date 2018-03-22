package jumpaku.fsc.snap.conicsection

import jumpaku.core.affine.Point
import jumpaku.core.affine.Vector
import jumpaku.fsc.snap.Grid
import jumpaku.fsc.snap.point.PointSnapper
import org.assertj.core.api.Assertions.*
import org.junit.Test

class ConicSectionSnapperTest {

    val w = 1280.0

    val h = 720.0

    val conicSectionSnapper = ConicSectionSnapper(
            PointSnapper(
                    Grid(
                            spacing = 50.0,
                            magnification = 2,
                            origin = Point.xy(w/2, h/2),
                            axis = Vector.K,
                            radian = 0.0,
                            fuzziness = 10.0,
                            resolution = 0),
                    minResolution = -5,
                    maxResolution = 5),
            ConjugateCombinator())

    @Test
    fun testSnap() {
        println("Snap")
        fail("Snap not implemented.")
    }

}