# JumpakuCurves

A library to identify geometric curves.

## setting

```gradle
repositories {
    maven {
        url "https://nexus.jumpaku.net/repository/maven-public/"
    }
}

dependencies {
    compile "jumpaku:core:0.10.3"
    compile "jumpaku:fsc:0.10.3"
}
```

## Example Application

```kt
package jumpaku

import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.stage.Stage
import jumpaku.core.curve.ParamPoint
import jumpaku.core.geom.Point
import jumpaku.fsc.generate.FscGenerator

fun main(args: Array<String>) {
    Application.launch(InputFsc::class.java, *args)
}

class InputFsc : Application() {

    private val data: MutableList<ParamPoint> = mutableListOf()

    private val canvas = Canvas(640.0, 480.0)

    override fun start(primaryStage: Stage) {
        canvas.setOnMousePressed { addData(it) }
        canvas.onMouseDragged = EventHandler<MouseEvent> { addData(it) }
        canvas.onMouseReleased = EventHandler<MouseEvent> { generateFsc(data); data.clear() }

        primaryStage.scene = Scene(Pane(canvas))
        primaryStage.show()
    }

    /**
     * Adds dragged mouse position data to [data].
     */
    private fun addData(e: MouseEvent) {
        val t = System.nanoTime() * 1e-9
        val x = e.x
        val y = e.y
        data.add(ParamPoint(Point.xy(e.x, e.y), t))
        canvas.graphicsContext2D.fillOval(x - 1, y - 1, 2.0, 2.0)
    }

    /**
     * Generates FSC from [data].
     */
    private fun generateFsc(data: List<ParamPoint>) {
        val fsc = FscGenerator().generate(data)
        val ctx = canvas.graphicsContext2D
        ctx.clearRect(0.0, 0.0, 640.0, 480.0)
        ctx.beginPath()
        fsc.toBeziers().forEach {
            val cp = it.controlPoints
            ctx.moveTo(cp[0].x, cp[0].y)
            ctx.bezierCurveTo(cp[1].x, cp[1].y, cp[2].x, cp[2].y, cp[3].x, cp[3].y)
        }
        ctx.stroke()
        fsc.evaluateAll(0.01).forEach { (x, y, _, r) ->
            ctx.strokeOval(x - r, y - r, 2 * r, 2 * r)
        }
    }
}
```
