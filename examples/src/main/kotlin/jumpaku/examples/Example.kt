package jumpaku.examples

import javafx.application.Application
import javafx.scene.layout.Pane
import jumpaku.curves.core.curve.arclength.Reparametrizer
import jumpaku.curves.core.curve.rationalbezier.ConicSection
import jumpaku.curves.core.geom.Point
import jumpaku.fxcomponents.colors.CudPalette
import jumpaku.fxcomponents.colors.fx
import jumpaku.fxcomponents.nodes.*
import tornadofx.App
import tornadofx.View
import tornadofx.group
import tornadofx.pane
import kotlin.math.sqrt


fun main(vararg args: String) = Application.launch(AppExample::class.java, *args)

class AppExample : App(ViewExample::class)

class ViewExample : View() {
    override val root: Pane = pane {
        val group = group {
            val R2 = sqrt(2.0)

            val cs = ConicSection(
                    Point.xy(200.0, 400.0),
                    Point.xy(200.0*(1 - R2/2), 200.0*(1 - R2/2)),
                    Point.xy(400.0, 200.0),
                    -R2/2)
            curve(cs) { stroke = CudPalette.BLACK.fx() }
            val r = Reparametrizer.of(cs, cs.domain.sample(100000))
            val a = listOf(
                    0.1,
                    0.2,
                    0.23423197731136688,
                    0.3,
                    0.3219386150484724,
                    0.36880698353408853,
                    0.39877659250419195,
                    0.4,
                    0.42042357458018836,
                    0.43792720933528817,
                    0.45502050031932484,
                    0.47386580342825335,
                    0.48061658402197577,
                    0.4848237183167176,
                    0.4880866244954987,
                    0.49085818174568274,
                    0.4933401081227524,
                    0.49564498700004356,
                    0.49784647668643145)

            val ts = listOf(listOf(0.0), a, listOf(0.5), a.asReversed().map { 1.0 - it }, listOf(1.0)).flatten()
            println(ts)
            fuzzyPoints(ts.map { cs(it).copy(r = 2.0) }) { stroke = CudPalette.RED.fx() }
        }
        fscUpdateControl {
            prefWidth = 1280.0
            prefHeight = 720.0
            onFscUpdated { e ->
                with(group) {
                    children.clear()
                    val s = e.fsc
                    curve(s) { stroke = CudPalette.RED.fx() }
                    fuzzyCurve(s) { stroke = CudPalette.BLUE.fx() }
                }
            }
        }
    }
}