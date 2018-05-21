package jumpaku.examples.blend

import io.vavr.control.Option
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import jumpaku.core.curve.bspline.BSpline
import jumpaku.core.json.parseJson
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

fun main(vararg args: String) = Application.launch(AppBlend::class.java, *args)

class AppBlend : App(ViewBlend::class)

class ViewBlend : View() {

    val generator = FscGenerator(
            degree = 3,
            knotSpan = 0.1,
            preparer = DataPreparer(0.1/3, 0.1, 0.1, 2),
            fuzzifier = LinearFuzzifier(0.004, 0.003))


    val blender = Blender(
            1.0/128,
            0.5,
            { it.grade }
    )

    override val root: Pane = pane {
        val group = group {  }
        curveControl {
            prefWidth = 1280.0
            prefHeight = 720.0

            val path = Paths.get("./fsc-test/src/test/resources/jumpaku/fsc/test/blend")
            for (i in 0..4) {
                val e = path.resolve("BlendExisting$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
                val o = path.resolve("BlendOverlapping$i.json").parseJson().flatMap { BSpline.fromJson(it) }.get()
                val result = blender.blend(e, o)
                path.resolve("BlendResult$i.json").toFile().apply { createNewFile() }.writeText(result.toString())
            }
            onCurveDone { e ->
                clear()
                group.update(FscGenerator().generate(e.data))
            }
        }
    }

    var existing: Option<BSpline> = Option.none()

    fun Group.update(overlap: BSpline){
        children.clear()
        existing.forEach { cubicFsc(it) { stroke = Color.BLACK } }
        cubicFsc(overlap) { stroke = Color.BLUE; strokeWidth = 1.0 }
        existing = existing.flatMap {
            val blend = blender.blend(it, overlap)
            println(blend.osm)
            blend.data.onEach {
                fuzzyPoints(it.map { it.point.copy(r = 10.0) }) { stroke = Color.GREEN }
            }.map {
                generator.generate(it)
            }
        }.orElse { existing }.orElse { Option.of(overlap) }
        existing.forEach { cubicFsc(it) { stroke = Color.RED } }
    }
}
