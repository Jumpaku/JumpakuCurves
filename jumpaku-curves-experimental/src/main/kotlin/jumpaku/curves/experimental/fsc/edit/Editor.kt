package jumpaku.curves.experimental.fsc.edit

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonElement
import jumpaku.commons.control.*
import jumpaku.commons.json.ToJson
import jumpaku.curves.core.curve.Interval
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.core.geom.Point
import jumpaku.curves.fsc.fragment.Fragment


class FscComponent(val graph: Component<Element.Id, Element>) : Map<Element.Id, Element> by graph, ToJson {

    fun connectors(): List<Element.Connector> = graph.mapNotNull { it.value as? Element.Connector }

    fun fragments(): List<Element.Identified> = graph.mapNotNull { it.value as? Element.Identified }

    data class FragmentWithConnectors(
            val fragment: BSpline,
            val front: Option<Point>,
            val back: Option<Point>
    )

    fun fragmentsWithConnectors(): List<FragmentWithConnectors> = graph
            .filterValues { it is Element.Identified }
            .map { (id, element) ->
                val f = element as Element.Identified
                val (front, back) = listOf(graph.prevOf(id), graph.nextOf(id)).map {
                    it.map { (graph.getValue(it) as Element.Connector).body }
                }
                FragmentWithConnectors(f.fragment, front, back)
            }

    override fun toJson(): JsonElement = jsonObject(
            "graph" to graph.map { (k, v) -> k.elementId.toJson() to v.toJson() }.toJsonArray())
}


class Editor(
        val nConnectorSamples: Int = 17,
        val connectionThreshold: Grade = Grade.FALSE,
        val blender: (BSpline, BSpline) -> Option<BSpline>,
        val fragmenter: (BSpline) -> List<Fragment>) {

    fun edit(overlapFsc: BSpline, fscComponents: List<FscComponent>): List<FscComponent> {
        val (merged, decomposed) = merge(overlapFsc, Graph.compose(fscComponents.map { it.graph }))
        val fragmented = fragment(merged)
        val resolved = resolveConnectivity(decomposed, fragmented)
        return cleanupConnector(resolved)
    }

    fun merge(overlap: BSpline, graph: Graph<Element.Id, Element>): Pair<BSpline, Graph<Element.Id, Element>> {
        val selected = graph.filterValues { element ->
            element is Element.Identified && blender(element.fragment, overlap).isDefined
        }
        var merged = overlap
        val removed = mutableSetOf<Element.Id>()
        selected.forEach { (id, element) ->
            blender((element as Element.Identified).fragment, merged)
                    .forEach { blended ->
                        merged = blended
                        removed += id
                    }
        }
        return Pair(merged, graph.remove(removed))
    }

    fun fragment(merged: BSpline): Component<Element.Id, Element> {
        val fragments = fragmenter(merged)
        val elements = fragments.mapIndexed { index, (interval, type) ->
            val (id, e) = when (type) {
                Fragment.Type.Move -> Element.identified(merged.restrict(interval))
                Fragment.Type.Stay -> {
                    val begin = if (index == 0) interval.begin else fragments[index - 1].interval.end
                    val l = fragments.lastIndex
                    val end = if (index == l) interval.end else fragments[index + 1].interval.begin
                    val ps = Interval(begin, end).sample(nConnectorSamples).map(merged)
                    val body = ps.minBy { it.r }!!.copy(r = ps.map { it.r }.average())
                    val first = optionWhen(index > 0) { merged(fragments[index - 1].interval.end) }
                    val last = optionWhen(index < l) { merged(fragments[index + 1].interval.begin) }
                    Element.connector(body, first, last)
                }
            }
            id to Component.OrderedElement(index, e)
        }
        return Component(elements.toMap(), false)
    }

    fun resolveConnectivity(
            decomposed: Graph<Element.Id, Element>,
            fragmented: Component<Element.Id, Element>
    ): List<Component<Element.Id, Element>> {
        val (first, _) = fragmented.first.orThrow()
        val (last, _) = fragmented.last.orThrow()
        var g = decomposed.compose(fragmented)
        g = g.vertices.map { evaluateConnection(it, first, g) }.filter { it.grade > connectionThreshold }
                .maxBy { it.grade }?.let { it.connect(g) } ?: g
        g = g.vertices.map { evaluateConnection(last, it, g) }.filter { it.grade > connectionThreshold }
                .maxBy { it.grade }?.let { it.connect(g) } ?: g
        return g.decompose()
    }

    fun cleanupConnector(resolved: List<Component<Element.Id, Element>>): List<FscComponent> =
            resolved.flatMap { component ->
                var g: Graph<Element.Id, Element> = component
                g = component.first.map { (id, e) ->
                    if (e is Element.Connector && id in g) g.updateValue(id, e.copy(front = None)) else g
                }.orDefault(g)
                g = component.last.map { (id, e) ->
                    if (e is Element.Connector && id in g) g.updateValue(id, e.copy(back = None)) else g
                }.orDefault(g)
                g.decompose()
            }.filter { c -> c.count { (_, e) -> e is Element.Identified } > 0 }.map { FscComponent(it) }

    /*
    override fun toJson(): JsonElement = jsonObject(
            "nConnectorSamples" to nConnectorSamples.toJson(),
            "connectionThreshold" to connectionThreshold.toJson(),
            "generator" to generator.toJson(),
            "blender" to blender.toJson(),
            "fragmenter" to fragmenter.toJson()
    )

    override fun toString(): String = toJsonString()
    */

    companion object {
        /*
        fun fromJson(json: JsonElement): Editor = Editor(
                json["nConnectorSamples"].int,
                Grade.fromJson(json["connectionThreshold"].asJsonPrimitive),
                Generator.fromJson(json["generator"]),
                Blender.fromJson(json["blender"]),
                Fragmenter.fromJson(json["fragmenter"])
        )
        */

        private data class Connection(
                val source: Element.Id,
                val destination: Element.Id,
                val grade: Grade = Grade.FALSE,
                val connector: Option<Element.Connector> = None
        ) {
            fun connect(graph: Graph<Element.Id, Element>): Graph<Element.Id, Element> {
                val s = graph[source]
                val d = graph[destination]
                val c = connector.orNull() ?: return graph
                return when {
                    s is Element.Connector && d is Element.Connector ->
                        graph.connect(source, destination) { c.withId() }
                    s is Element.Connector && d is Element.Identified ->
                        graph.updateValue(source, c).insert(insertedEdges = setOf(Graph.Edge(source, destination)))
                    s is Element.Identified && d is Element.Connector ->
                        graph.updateValue(destination, c).insert(insertedEdges = setOf(Graph.Edge(source, destination)))
                    else -> graph
                }
            }
        }

        private fun evaluateConnection(
                srcId: Element.Id,
                destId: Element.Id,
                graph: Graph<Element.Id, Element>
        ): Connection {
            val default = Connection(srcId, destId)
            val src = graph[srcId]
            val dest = graph[destId]
            return when {
                // src is not in graph or dest is not in graph
                src == null || dest == null ||
                        // src is not last
                        graph.nextOf(srcId).isDefined ||
                        // dest is not first
                        graph.prevOf(destId).isDefined ||
                        // src and dest are fragments
                        src is Element.Identified && dest is Element.Identified ||
                        // src is complete connector and dest is fragment
                        src is Element.Connector && src.back is None && dest is Element.Identified ||
                        // src is fragment and dest is complete connector
                        src is Element.Identified && dest is Element.Connector && dest.front is None
                -> default
                // src is last connector and dest is first connector
                src is Element.Connector && dest is Element.Connector -> default.copy(
                        grade = src.body.isPossible(dest.body),
                        connector = Some(Element.Connector(src.body.middle(dest.body), src.front, dest.back))
                )
                // src is incomplete connector and dest is fragment
                src is Element.Connector && src.back is Some && dest is Element.Identified -> default.copy(
                        grade = src.back.value.isPossible(dest.front),
                        connector = Some(src.copy(back = Some(dest.front)))
                )
                // src is fragment and dest is incomplete connector
                src is Element.Identified && dest is Element.Connector && dest.front is Some -> default.copy(
                        grade = src.back.isPossible(dest.front.value),
                        connector = Some(dest.copy(front = Some(src.back)))
                )
                else -> error("")
            }
        }
    }
}
