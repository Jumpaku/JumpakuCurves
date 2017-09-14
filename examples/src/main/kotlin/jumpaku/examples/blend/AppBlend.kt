package jumpaku.examples.blend

import io.vavr.API
import io.vavr.Tuple2
import io.vavr.control.Option
import io.vavr.control.Try
import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fuzzy.Grade
import jumpaku.fsc.blend.BlendResult
import jumpaku.fsc.blend.Blender
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fxcomponents.view.CurveInput
import jumpaku.fxcomponents.view.cubicFsc
import tornadofx.App
import tornadofx.Scope
import tornadofx.View
import java.nio.file.Paths

fun main(vararg args: String) = Application.launch(AppBlend::class.java, *args)

class AppBlend : App(ViewBlend::class)

class ViewBlend : View() {

    override val scope: Scope = Scope()

    override val root: Pane

    private val curveInput: CurveInput

    init {
        curveInput = CurveInput(scope = scope)
        root = curveInput.root
        subscribe<CurveInput.CurveDoneEvent> {
            if (it.data.size() > 2) {
                val fsc = FscGenerator(3, 0.1).generate(it.data)
                with(curveInput.contents) {
                    children.clear()
                    render(fsc)
                }
            }
        }
    }

    var existing: Option<BSpline> = Option.none()
    var i = 0
    val path = Paths.get("./")

    private fun Parent.render(overlapping: BSpline) {
        cubicFsc(overlapping) { stroke = Color.GREEN }
        if (existing.isEmpty){
            existing = Option.of(overlapping)
            return
        }
        existing.forEach { cubicFsc(it) { stroke = Color.BLUE } }

        Try.run { existing.map {
            val result = Blender().blend(it, overlapping)
            printOsm(result)
            result.blended.forEach { cubicFsc(it) { stroke = Color.RED } }

            path.resolve("BlendExisting$i.json").toFile().writeText(it.toString())
            path.resolve("BlendOverlapping$i.json").toFile().writeText(overlapping.toString())
            path.resolve("BlendResult$i.json").toFile().writeText(result.toString())
            ++i
        } }.onFailure {
            println("the following fscs caused a blending error")
            existing.forEach { println(it) }
            println(overlapping)
        }.getOrElseThrow { e -> throw e }
    }

    private fun printOsm(result: BlendResult) {
        val (osm, path, _) = result
        print("    ")
        (0..osm.rowLastIndex).forEach { print("%4d".format(it))}
        println()
        API.For(0..osm.rowLastIndex, 0..osm.columnLastIndex).`yield` { i, j ->
            val prefix = if (j == 0) "%3d| ".format(i) else ","
            val symbol = when {
                path.map { Tuple2(i, j) in it.path }.getOrElse(false) -> "  @"
                osm[i, j] > Grade.FALSE -> "  +"
                else -> "   "
            }
            val postfix = if (j == osm.columnLastIndex) ",%n".format() else ""
            prefix + symbol + postfix
        }.forEach(::print)
    }
}
