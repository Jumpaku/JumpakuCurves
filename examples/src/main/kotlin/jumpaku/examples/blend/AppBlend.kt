package jumpaku.examples.blend

import io.vavr.API
import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.control.Option
import io.vavr.control.Try
import javafx.application.Application
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.ParamPoint
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.fuzzy.Grade
import jumpaku.core.json.parseToJson
import jumpaku.fsc.blend.Blender
import jumpaku.fsc.blend.OverlappingMatrix
import jumpaku.fsc.blend.OverlappingPath
import jumpaku.fsc.blend.blendResult
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fxcomponents.view.CurveInput
import jumpaku.fxcomponents.view.cubicFsc
import tornadofx.App
import tornadofx.Scope
import tornadofx.View

fun main(vararg args: String) = Application.launch(AppBlend::class.java, *args)

class AppBlend : App(ViewBlend::class)

class ViewBlend : View(){

    override val scope: Scope = Scope()

    override val root: Pane

    private val curveInput: CurveInput

    init {
        curveInput = CurveInput(scope = scope)
        root = curveInput.root
        subscribe<CurveInput.CurveDoneEvent> {
            render(it.data)
        }
    }

    private var existing: Option<BSpline> = Option.none()

    private fun render(data: Array<ParamPoint>) {
        if (data.size() <= 2){
            return
        }
        with(curveInput.contents) {
            children.clear()
            existing.forEach { cubicFsc(it) { stroke = Color.BLUE } }
            val overlapping = FscGenerator(3, 0.1).generate(Array.ofAll(data))
            cubicFsc(overlapping) { stroke = Color.GREEN }

            if(existing.isEmpty){
                existing = Option.of(overlapping)
                return
            }

            val samplingSpan = 1.0/128
            val b = Blender(samplingSpan, 0.5, FscGenerator(), { _ -> grade.value })

            Try.run {
                existing.map {
                    val (osm, path, blended) = b.blend(existing.get(), overlapping)
                    printOsm(osm, path)
                    blended.forEach { cubicFsc(it) { stroke = Color.RED } }
                }
            }.onFailure {
                println("the following fscs caused a blending error")
                existing.forEach { println(it) }
                println(overlapping)
                println(it.message)
            }.getOrElseThrow { t -> throw t }
        }
    }

    private fun printOsm(osm: OverlappingMatrix, path: Option<OverlappingPath>) {
        print("    ")
        (0..osm.rowLastIndex).forEach { print("%4d".format(it))}
        println()
        API.For(0..osm.rowLastIndex, 0..osm.columnLastIndex)
                .`yield` { i, j ->
                    val prefix = if (j == 0) "%3d| ".format(i) else ","
                    val symbol = when {
                        path.map { Tuple2(i, j) in it.path } .getOrElse(false) -> "  @"
                        osm[i, j] > Grade.FALSE -> "  +"
                        else -> "   "
                    }
                    val postfix = if (j == osm.columnLastIndex) ",%n".format() else ""
                    prefix + symbol + postfix
                }
                .forEach(::print)
    }
}
