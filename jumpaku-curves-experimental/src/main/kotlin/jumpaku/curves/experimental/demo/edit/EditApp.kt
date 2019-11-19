package jumpaku.curves.experimental.demo.edit

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonElement
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import javafx.stage.WindowEvent
import jumpaku.commons.control.orDefault
import jumpaku.commons.history.Command
import jumpaku.commons.history.History
import jumpaku.commons.json.parseJson
import jumpaku.curves.core.curve.Curve
import jumpaku.curves.core.curve.Differentiable
import jumpaku.curves.core.curve.ParamPoint
import jumpaku.curves.core.curve.arclength.ReparametrizedCurve
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.curve.polyline.Polyline
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.geom.times
import jumpaku.curves.core.transform.Calibrate
import jumpaku.curves.core.transform.Rotate
import jumpaku.curves.experimental.fsc.edit.Editor
import jumpaku.curves.experimental.fsc.edit.FscGraph
import jumpaku.curves.experimental.fsc.edit.FscPath
import jumpaku.curves.fsc.DrawingStroke
import jumpaku.curves.fsc.blend.BlendGenerator
import jumpaku.curves.fsc.blend.Blender
import jumpaku.curves.fsc.fragment.Chunk
import jumpaku.curves.fsc.fragment.Fragmenter
import jumpaku.curves.fsc.generate.Fuzzifier
import jumpaku.curves.fsc.generate.Generator
import jumpaku.curves.fsc.identify.primitive.CurveClass
import jumpaku.curves.fsc.identify.primitive.Identifier
import jumpaku.curves.fsc.identify.primitive.IdentifyResult
import jumpaku.curves.fsc.identify.primitive.reference.CircularGenerator
import jumpaku.curves.fsc.identify.primitive.reference.EllipticGenerator
import jumpaku.curves.fsc.identify.primitive.reference.LinearGenerator
import jumpaku.curves.fsc.identify.primitive.reference.Reference
import jumpaku.curves.fsc.identify.primitive.reparametrize
import jumpaku.curves.fsc.snap.Grid
import jumpaku.curves.fsc.snap.point.MFGS
import jumpaku.curves.graphics.*
import jumpaku.curves.graphics.fx.DrawingControl
import jumpaku.curves.graphics.fx.DrawingEvent
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Path2D
import java.nio.file.Path
import java.nio.file.Paths


object ExpSettings {

    val width = 1600.0

    val height = 900.0

    val identifier = Original7Identifier(nSamples = 25, nFmps = 15)

    val baseGrid = Grid(
            baseSpacing = 64.0,
            baseFuzziness = 8.0,
            magnification = 2,
            origin = Point.xy(width / 2, height / 2),
            rotation = Rotate(Vector.K, 0.0)
    )

    val pointSnapper = MFGS(
            minResolution = -5,
            maxResolution = 7
    )

    class Original7Identifier(val nSamples: Int = 25, override val nFmps: Int = 15) : Identifier {

        sealed class Result {
            abstract val curve: Curve

            class L(override val curve: ConicSection) : Result()
            class C(override val curve: Reference) : Result()
            class CA(override val curve: Reference) : Result()
            class E(override val curve: Reference) : Result()
            class EA(override val curve: Reference) : Result()
            class FC(override val curve: BSpline) : Result()
            class FO(override val curve: BSpline) : Result()
        }


        override fun <C : Curve> identify(fsc: ReparametrizedCurve<C>): IdentifyResult {
            val s = fsc.originalCurve
            val refL = LinearGenerator().generateBeginEnd(fsc)
            val t0 = s.domain.begin
            val t2 = s.domain.sample(nSamples).maxBy { s(t0).distSquare(s(it)) }!!
            val refC = CircularGenerator(nSamples).generate(fsc, t0, t2)
            val refE = EllipticGenerator(nSamples).generate(fsc, t0, t2)
            val (pL, pC, pE) = listOf(refL, refC, refE).map { fsc.isPossible(it) }
            val pClosed = isClosed(fsc)
            val grades = hashMapOf(
                    CurveClass.LineSegment to (pL),
                    CurveClass.Circle to (pClosed and !pL and pC),
                    CurveClass.CircularArc to (!pClosed and !pL and pC),
                    CurveClass.Ellipse to (pClosed and !pL and !pC and pE),
                    CurveClass.EllipticArc to (!pClosed and !pL and !pC and pE),
                    CurveClass.ClosedFreeCurve to (pClosed and !pL and !pC and !pE),
                    CurveClass.OpenFreeCurve to (!pClosed and !pL and !pC and !pE))
            return IdentifyResult(grades, refL, refC, refE)
        }

        fun identify(fsc: BSpline): Result = identify(reparametrize(fsc)).run {
            when (curveClass) {
                CurveClass.Point, CurveClass.LineSegment -> Result.L(linear.base)
                CurveClass.Circle -> Result.C(circular)
                CurveClass.CircularArc -> Result.CA(circular)
                CurveClass.Ellipse -> Result.E(elliptic)
                CurveClass.EllipticArc -> Result.EA(elliptic)
                CurveClass.ClosedFreeCurve -> Result.FC(fsc.close())
                CurveClass.OpenFreeCurve -> Result.FO(fsc)
            }
        }

        override fun toJson(): JsonElement = throw UnsupportedOperationException()
    }

    object Snapper {

        fun snap(
                identifiedFragment: FscPath.FragmentWithConnectors,
                result: Original7Identifier.Result
        ): Original7Identifier.Result {
            val (identified, front, back) = identifiedFragment
            val (begin, end) = identified.evaluateAll(2)
            return when (result) {
                is Original7Identifier.Result.L, is Original7Identifier.Result.CA, is Original7Identifier.Result.EA ->
                    snapLCAEA(result, front.orDefault(begin), back.orDefault(end))
                is Original7Identifier.Result.C, is Original7Identifier.Result.E ->
                    snapCE(result, front.orDefault(begin))
                else -> result
            }
        }

        fun snapLCAEA(
                result: Original7Identifier.Result,
                frontStop: Point,
                backStop: Point
        ): Original7Identifier.Result {
            val grid = baseGrid
            val mfgs = pointSnapper
            val (snappedFront, snappedBack) = listOf(frontStop, backStop).map {
                mfgs.snap(grid, it).map { it.worldPoint(grid) }.orDefault(it)
            }
            val (begin, end) = result.curve.evaluateAll(2)
            val transform = Calibrate.similarityWithNormal(
                    begin to snappedFront, end to snappedBack, Vector.K to Vector.K
            )
            return when (result) {
                is Original7Identifier.Result.L -> result.curve.run {
                    Original7Identifier.Result.L(transform(transform))
                }
                is Original7Identifier.Result.CA -> result.curve.run {
                    Original7Identifier.Result.CA(Reference(base.transform(transform), domain))
                }
                is Original7Identifier.Result.EA -> result.curve.run {
                    Original7Identifier.Result.EA(Reference(base.transform(transform), domain))
                }
                else -> error("result is not open conic section")
            }
        }

        fun snapCE(result: Original7Identifier.Result, frontStop: Point): Original7Identifier.Result {
            val grid = baseGrid
            val mfgs = pointSnapper
            val r = (result.curve as Reference)
            val center = r.base.center().orThrow()
            val (snappedFront, snappedBack) = listOf(frontStop, center).map {
                mfgs.snap(grid, it).map { it.worldPoint(grid) }.orDefault(it)
            }
            val begin = r.evaluateAll(2).first()
            val transform = Calibrate.similarityWithNormal(
                    begin to snappedFront, center to snappedBack, Vector.K to Vector.K
            )
            return when (result) {
                is Original7Identifier.Result.C -> result.curve.run {
                    Original7Identifier.Result.C(Reference(base.transform(transform), domain))
                }
                is Original7Identifier.Result.E -> result.curve.run {
                    Original7Identifier.Result.E(Reference(base.transform(transform), domain))
                }
                else -> error("result is not closed conic section")
            }
        }
    }

    val generator: Generator = Generator(
            degree = 3,
            knotSpan = 0.1,
            fillSpan = 0.025,
            extendInnerSpan = 0.175,
            extendOuterSpan = 0.175,
            extendDegree = 2,
            fuzzifier = Fuzzifier.Linear(
                    velocityCoefficient = 0.0085,
                    accelerationCoefficient = 0.0075))
    val blender = Blender(
            samplingSpan = 0.01,
            blendingRate = 0.65,
            threshold = Grade.FALSE)
    val blendGenerator = BlendGenerator(
            degree = generator.degree,
            knotSpan = generator.knotSpan,
            bandWidth = blender.samplingSpan,
            extendInnerSpan = generator.extendInnerSpan,
            extendOuterSpan = generator.extendOuterSpan,
            extendDegree = generator.extendDegree,
            fuzzifier = generator.fuzzifier)
    val fragmenter = Fragmenter(
            threshold = Chunk.Threshold(
                    necessity = 0.45,
                    possibility = 0.8),
            chunkSize = 6,
            minStayTimeSpan = 0.05)
    val editor: Editor = Editor(
            nConnectorSamples = 17,
            connectionThreshold = Grade.FALSE,
            merger = Editor.mergerOf(blender, blendGenerator),
            fragmenter = Editor.fragmenterOf(fragmenter))
}

class FscGraphState(val fixed: FscGraph = FscGraph(), val editing: FscGraph = FscGraph(), val input: List<List<DrawingStroke>> = listOf(listOf()))

fun main(args: Array<String>) = Application.launch(EditExperiment::class.java, *args)

class EditExperiment : Application() {

    var history: History<FscGraphState> = History<FscGraphState>().run { exec { loadData() }}//FscGraphState() } }

    val curveControl = DrawingControl(ExpSettings.width, ExpSettings.height).apply {
        addEventHandler(DrawingEvent.DRAWING_DONE) {
            val drawingStroke = it.drawingStroke
            if (drawingStroke.domain.span < 0.1) return@addEventHandler
            val s = ExpSettings.generator.generate(it.drawingStroke)
            update(Command.Do {
                it.map {
                    FscGraphState(fixed = it.fixed,
                            editing = ExpSettings.editor.edit(s, it.editing),
                            input = it.input.run { dropLast(1) + listOf((last() + listOf(drawingStroke))) })
                }.orThrow()
            })
        }
    }

    override fun start(primaryStage: Stage) {

        primaryStage.apply {
            scene = Scene(curveControl)
            scene.addEventHandler(KeyEvent.KEY_PRESSED) {
                update(when (it.code) {
                    KeyCode.C -> Command.Do { FscGraphState() }
                    KeyCode.U -> Command.Undo
                    KeyCode.S -> {
                        history.current.forEach { storeData(it) }
                        Command.Do { it.orDefault { FscGraphState() } }
                    }
                    KeyCode.ENTER -> Command.Do<FscGraphState> {
                        it.map {
                            FscGraphState(fixed = it.fixed.compose(it.editing),
                                    editing = FscGraph(),
                                    input = it.input + listOf(listOf()))
                        }.orThrow()
                    }
                    else -> Command.Do { it.orDefault { FscGraphState() } }
                })
            }
            addEventHandler(WindowEvent.WINDOW_SHOWN) { update() }
            show()
        }
    }

    fun update(command: Command<FscGraphState> = Command.Do { it.orDefault(FscGraphState()) }) {
        history = history.exec(command)
        history.current.forEach { draw(it) }
        history.current.forEach { println(it.input.map { it.size }) }

    }

    fun draw(state: FscGraphState) {
        curveControl.updateGraphics2D {
            clearRect(0.0, 0.0, ExpSettings.width, ExpSettings.height)
            drawGrids()
            drawInactiveObjects(state.fixed)
            drawActiveObjects(state.editing)
        }
    }
}

private object ExpStyles

fun Graphics2D.drawActiveObjects(objects: FscGraph) {
    objects.decompose().forEach { component ->
        component.fragments().forEach { drawPoints(it.fragment.evaluateAll(0.01), DrawStyle(Color.GRAY)) }
    }
    objects.decompose().flatMap { it.fragmentsWithConnectors() }.forEach {
        val r = ExpSettings.identifier.identify(it.fragment)
        val s = ExpSettings.Snapper.snap(it, r)
        drawResult(s, Color.RED)
    }
    objects.decompose().forEach { component ->
        component.connectors().forEach {
            val ps = it.front + it.body + it.back
            drawPoints(ps, DrawStyle(Color.BLUE))
            drawPolyline(Polyline(ps.mapIndexed { i, p -> ParamPoint(p, i.toDouble()) }), DrawStyle(Color.BLUE))
        }
    }
}

fun Graphics2D.drawInactiveObjects(objects: FscGraph) {
    objects.decompose().forEach { component ->
        component.fragments().forEach { drawPoints(it.fragment.evaluateAll(0.01), DrawStyle(Color(127, 127, 127, 63))) }
    }
    objects.decompose().flatMap { it.fragmentsWithConnectors() }.forEach {
        val r = ExpSettings.identifier.identify(it.fragment)
        val s = ExpSettings.Snapper.snap(it, r)
        drawResult(s, Color(127, 0, 0, 63))
    }
    objects.decompose().forEach { component ->
        component.connectors().forEach {
            val ps = it.front + it.body + it.back
            drawPoints(ps, DrawStyle(Color(0, 0, 127, 63)))
            drawPolyline(Polyline(ps.mapIndexed { i, p -> ParamPoint(p, i.toDouble()) }), DrawStyle(Color(0, 0, 127, 63)))
        }
    }
}

fun Graphics2D.drawResult(result: ExpSettings.Original7Identifier.Result, color: Color) {
    val drawStyle = DrawStyle(color, BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
    val fillStyle = FillStyle(color)
    when (result) {
        is ExpSettings.Original7Identifier.Result.L -> {
            drawConicSection(result.curve, drawStyle)
            drawArrow(result.curve, fillStyle)
        }
        is ExpSettings.Original7Identifier.Result.C, is ExpSettings.Original7Identifier.Result.E -> {
            (result.curve as? Reference)?.run {
                drawConicSection(base, drawStyle)
                drawConicSection(complement, drawStyle)
                drawArrow(complement.reverse(), fillStyle)
            }
        }
        is ExpSettings.Original7Identifier.Result.FC, is ExpSettings.Original7Identifier.Result.FO -> {
            drawCubicBSpline(result.curve as BSpline, drawStyle)
            drawArrow(result.curve as BSpline, fillStyle)
        }
        else -> {
            val r = result.curve as Reference
            val c = r.base.complement().restrict((2 - r.domain.end).coerceIn(0.0, 1.0), 1.0)
            drawConicSection(r.base, drawStyle)
            drawConicSection(c, drawStyle)
            drawArrow(if ((2 - r.domain.end).coerceIn(0.0, 1.0) < 0.999) c.reverse() else r.base, fillStyle)
        }
    }
}

fun <C> Graphics2D.drawArrow(curve: C, style: FillStyle) where C : Curve, C : Differentiable {
    val (p, dp) = curve.run { evaluate(domain.end) to differentiate(domain.end) }
    dp.normalize().onSuccess { v ->
        v.cross(Vector.K).normalize().onSuccess { u ->
            fillShape(Path2D.Double().apply {
                (p - 14.0 * v).run { moveTo(x, y) }
                (p - 14.0 * v + 6.0 * u).run { lineTo(x, y) }
                (p + 1.5 * u).run { lineTo(x, y) }
                (p - 1.5 * u).run { lineTo(x, y) }
                (p - 14.0 * v - 6.0 * u).run { lineTo(x, y) }
                closePath()
            }, style)
        }
    }
}

fun Graphics2D.drawGrids() {
    val base = ExpSettings.baseGrid
    val w = ExpSettings.width
    val h = ExpSettings.height
    for (r in sequenceOf(2, 0, -2)) {
        drawGrid(base, r, 0.0, 0.0, w, h, DrawStyle(Color.GRAY, BasicStroke(Math.pow(2.0, -r * 0.5).toFloat())))
    }
}

fun storeData(state: FscGraphState, dir: Path = Paths.get("./")) {
    dir.resolve("fscGraphState.json").toFile().writeText(state.run {
        jsonObject(
                "input" to input.map {
                    it.map { it.toJson() }.toJsonArray()
                }.toJsonArray(),
                "fixed" to fixed.toJson(),
                "editing" to editing.toJson()).toString()
    })
}

fun loadData(dir: Path = Paths.get("./")): FscGraphState =
        dir.resolve("fscGraphState.json").parseJson().tryMap { json ->
            FscGraphState(
                    input = json["input"].array.map { it.array.map { DrawingStroke.fromJson(it) } },
                    fixed = FscGraph.fromJson(json["fixed"]),
                    editing = FscGraph.fromJson(json["editing"])
            )
        }.orThrow()

