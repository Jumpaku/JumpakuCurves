# JumpakuCurves

A library to identify geometric curves.

## Setting

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

## Authors

* Ito Tomohiko
* Hatamoto Naoya

## Releases

* 2019-01-19 ver. 0.11.0
* 2018-10-15 ver. 0.10
* 2018-08-19 ver. 0.9
* 2018-05-21 ver. 0.8
* 2018-04-27 ver. 0.7
* 2018-03-31 ver. 0.6
* 2018-03-24 ver. 0.5
* 2017-12-09 ver. 0.4
* 2017-11-03 ver. 0.3
* 2017-09-14 ver. 0.2
* 2017-09-02 ver. 0.1
