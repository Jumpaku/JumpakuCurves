package jumpaku.examples.snap

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.core.util.orDefault
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.identify.primitive.CurveClass
import jumpaku.curves.fsc.identify.primitive.Open4Identifier
import jumpaku.curves.fsc.identify.primitive.reparametrize
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.conicsection.ConicSectionSnapper
import jumpaku.curves.fsc.snap.conicsection.ConjugateBox
import jumpaku.curves.fsc.snap.conicsection.ConjugateCombinator
import jumpaku.curves.fsc.snap.point.MFGS
import jumpaku.fxcomponents.nodes.*
import tornadofx.*


fun main(vararg args: String) = Application.launch(AppSnap::class.java, *args)

class AppSnap : App(ViewSnap::class)

class ViewSnap : View() {

    val w = 1280.0
    val h = 720.0

    val generator = Generator(
            degree = 3,
            knotSpan = 0.1)

    val grid = Grid(
            baseSpacing = 64.0,
            magnification = 2,
            origin = Point.xy(w/2, h/2),
            rotation = Rotate(Vector.K, 0.0),
            baseFuzziness = 16.0)

    val conicSectionSnapper = ConicSectionSnapper(
            MFGS(minResolution = -5, maxResolution = 5),
            ConjugateCombinator())

    val identifier = Open4Identifier(nSamples = 25)

    //val xxx = "EA2"
    //val cc = CurveClass.EllipticArc
    override val root: Pane = pane {
        val group = group {
            grid(grid, 0.0, 0.0, w, h) { stroke = Color.BLACK }
            //val cs = File("./fsc-test/src/test/resources/jumpaku/fsc/test/snap/conicsection/ConicSection$xxx.json")
            //        .parseJson().tryMap { ConicSection.fromJson(it) }.orThrow()
            //drawSnapping(cs, cc)

        }
        curveControl {
            prefWidth = w
            prefHeight = h
            onCurveDone {
                clear()
                with(group) {
                    children.clear()
                    update(generator.generate(it.data))
                }
            }
        }
    }

    /*fun Group.drawSnapping(cs: ConicSection, curveClass: CurveClass) {
        fuzzyCurve(cs) { stroke = Color.BLACK }
        val snapped = conicSectionSnapper.snap(grid, cs, curveClass, evaluator = ConicSectionSnapper.evaluateWithReference(cs, 15))
        File("./fsc-test/src/test/resources/jumpaku/fsc/test/snap/conicsection/SnapResult$xxx.json")
                .writeText(snapped.toString())
        conjugateBox(ConjugateBox.ofConicSection(snapped.snappedConicSection.orDefault { cs })) { stroke = Color.GREEN }
        snapped.candidates.reversed().forEachIndexed { i, (grade, candidate) ->
            if (i < snapped.candidates.lastIndex) return@forEachIndexed
            println(grade)
            val color = Color.hsb(0.0, 0.3 + i*0.7/snapped.candidates.size, 1.0)
            curve(cs.transform(candidate.transform)) { stroke = color }
            candidate.featurePoints.forEach {
                val c = it.source
                val s = it.target
                s.forEach({
                    snappedPoint(grid, it) { stroke = color; fill = color }
                }, {
                    circle(c.x, c.y, 3.0) { stroke = color; fill = color }
                })
            }
        }
    }*/

    fun Group.update(fsc: BSpline) {
        grid(grid, 0.0, 0.0, w, h) { stroke = Color.BLACK }
        cubicFsc(fsc) { stroke = Color.BLACK }

        val identifyResult = identifier.identify(reparametrize(fsc))
        println(identifyResult.curveClass)
        if (identifyResult.curveClass.isFreeCurve) {
            cubicFsc(fsc.toCrisp()) { stroke = Color.RED }
            return
        }
        val cs: ConicSection = identifyResult.curveClass.let {
            when(it) {
                CurveClass.LineSegment -> identifyResult.linear.base
                CurveClass.CircularArc -> identifyResult.circular.base
                CurveClass.EllipticArc -> identifyResult.elliptic.base
                else -> kotlin.error("")
            }
        }
        val snapped = conicSectionSnapper.snap(grid, cs, identifyResult.curveClass)

        conjugateBox(ConjugateBox.ofConicSection(snapped.snappedConicSection.orDefault { cs })) { stroke = Color.GREEN }
        //curve(cs) { stroke = Color.BLUE }
        curve(snapped.snappedConicSection.orDefault { cs }) { stroke = Color.RED }
        snapped.snappedConicSection.forEach {
            snapped.candidates[0].candidate.featurePoints.forEach {
                val c = it.source
                val s = it.target
                s.forEach({
                    snappedPoint(grid, it) { stroke = Color.RED; fill = Color.RED }
                }, {
                    circle(c.x, c.y, 3.0) { stroke = Color.RED; fill = Color.RED }
                })
            }
        }

        /*snapped.candidates.reversed().forEachIndexed { i, candidate ->
            val color = Color.hsb(i*180.0/snapped.candidates.size, 1.0, 1.0)
            curve(cs.transform(candidate.transform)) { stroke = color }
            candidate.featurePoints.forEach {
                val c = it.source
                val s = it.target
                s.forNone { circle(c.x, c.y, 3.0) { stroke = color; fill = color } }
                s.forEach { snappedPoint(grid, it) { stroke = color; fill = color } }
            }
            if (i == snapped.candidates.lastIndex) {
                conjugateBox(ConjugateBox.ofConicSection(snapped.snappedConicSection)) { stroke = Color.GREEN }
            }
        }*/
    }
}

