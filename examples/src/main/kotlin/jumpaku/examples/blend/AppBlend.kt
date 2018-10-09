package jumpaku.examples.blend

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
import jumpaku.core.util.Option
import jumpaku.core.util.none
import jumpaku.core.util.orOption
import jumpaku.core.util.some
import jumpaku.fsc.blend.Blender
import jumpaku.fsc.generate.DataPreparer
import jumpaku.fsc.generate.FscGenerator
import jumpaku.fsc.generate.LinearFuzzifier
import jumpaku.fxcomponents.nodes.*
import tornadofx.App
import tornadofx.View
import tornadofx.group
import tornadofx.pane
import java.io.File
import java.nio.file.Paths
import kotlin.system.measureNanoTime

fun main(vararg args: String) = Application.launch(AppBlend::class.java, *args)

class AppBlend : App(ViewBlend::class)

class ViewBlend : View() {

    val generator = FscGenerator(
            degree = 3,
            knotSpan = 0.1)

    val blender = Blender(
            1.0/128,
            0.5,
            { it.grade })

    override val root: Pane = pane {
        val group = group {  }
        curveControl {
            prefWidth = 1280.0
            prefHeight = 720.0

            /*val path = Paths.get("./fsc-test/src/test/resources/jumpaku/fsc/test/blend")
            for (i in 0..4) {
                val e = path.resolve("BlendExisting$i.json").parseJson().tryFlatMap { BSpline.fromJson(it) }.orThrow()
                val o = path.resolve("BlendOverlapping$i.json").parseJson().tryFlatMap { BSpline.fromJson(it) }.orThrow()
                val result = blender.blend(e, o)
                path.resolve("BlendResult$i.json").toFile().apply { createNewFile() }.writeText(result.toString())
            }*/
            onCurveDone { e ->
                clear()
                group.update(FscGenerator().generate(e.data))
            }
        }
    }

    var existing: Option<BSpline> = none()

    fun Group.update(overlap: BSpline){
        children.clear()
        existing.forEach { cubicFsc(it) { stroke = Color.BLACK } }
        cubicFsc(overlap) { stroke = Color.BLUE; strokeWidth = 1.0 }
        existing = existing.flatMap {
            val blend = blender.blend(it, overlap)
            //println(blend.osm)
            println("${blend.osm.rowSize} x ${blend.osm.columnSize}")
            println("blend: ${measureNanoTime { blender.blend(it, overlap) }*1e-9}")
            blend.data.onEach {
                fuzzyPoints(it.map { it.point }) { stroke = Color.GREEN }
            }.map {
                println("gen: ${measureNanoTime { generator.generate(it) }*1e-9}")
                generator.generate(it)
            }
        }.orOption { existing }.orOption { some(overlap) }
        existing.forEach { cubicFsc(it) { stroke = Color.RED } }
    }
}
