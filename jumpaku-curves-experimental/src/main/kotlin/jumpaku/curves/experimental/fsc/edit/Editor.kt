package jumpaku.curves.experimental.fsc.edit

import jumpaku.commons.control.*
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.fuzzy.Grade
import jumpaku.curves.fsc.fragment.Fragment


class Editor(
        val nConnectorSamples: Int = 17,
        val connectionThreshold: Grade = Grade.FALSE,
        val blender: (BSpline, BSpline) -> Option<BSpline>,
        val fragmenter: (BSpline) -> List<Fragment>) {

    fun edit(overlapFsc: BSpline, fscPaths: List<FscPath>): List<FscPath> {
        val (merged, decomposed) = merge(overlapFsc, FscGraph.compose(fscPaths))
        val fragmented = fragment(merged)
        val resolved = resolveConnectivity(decomposed, fragmented)
        return cleanupConnector(resolved)
    }

    fun merge(overlap: BSpline, graph: FscGraph): Pair<BSpline, FscGraph> {
        val selected = graph.filterValues { element ->
            element is Element.Target && blender(element.fragment, overlap).isDefined
        }
        var merged = overlap
        val removed = mutableSetOf<Id>()
        selected.forEach { (id, element) ->
            blender((element as Element.Target).fragment, merged)
                    .forEach { blended ->
                        merged = blended
                        removed += id
                    }
        }
        return Pair(merged, graph.remove(removed))
    }

    fun fragment(merged: BSpline): FscPath {
        val fragments = fragmenter(merged)
        val elements = fragments.mapIndexed { index, (interval, type) ->
            val (id, e) = when (type) {
                Fragment.Type.Move -> Element.target(merged.restrict(interval))
                Fragment.Type.Stay -> {
                    val ps = interval.sample(nConnectorSamples).map(merged)
                    val body = ps.minBy { it.r }!!.copy(r = ps.map { it.r }.average())
                    val first = optionWhen(index > 0) { merged(fragments[index - 1].interval.end) }
                    val last = optionWhen(index < fragments.lastIndex) { merged(fragments[index + 1].interval.begin) }
                    Element.connector(body, first, last)
                }
            }
            id to e
        }
        return FscPath.openPath(elements)
    }

    fun resolveConnectivity(decomposed: FscGraph, fragmented: FscPath): List<FscPath> {
        val (first, _) = fragmented.first.orThrow()
        val (last, _) = fragmented.last.orThrow()
        var g = decomposed.compose(fragmented)
        g = g.vertices.map { evaluateConnection(it, first, g) }.filter { it.grade > connectionThreshold }
                .maxBy { it.grade }?.let { it.connect(g) } ?: g
        g = g.vertices.map { evaluateConnection(last, it, g) }.filter { it.grade > connectionThreshold }
                .maxBy { it.grade }?.let { it.connect(g) } ?: g
        return g.decompose()
    }

    fun cleanupConnector(resolved: List<FscPath>): List<FscPath> =
            resolved.flatMap { component ->
                var g: FscGraph = component
                g = component.first.map { (id, e) ->
                    if (e is Element.Connector && id in g) g.updateValue(id, e.copy(front = None)) else g
                }.orDefault(g)
                g = component.last.map { (id, e) ->
                    if (e is Element.Connector && id in g) g.updateValue(id, e.copy(back = None)) else g
                }.orDefault(g)
                g.decompose()
            }.filter { c -> c.count { (_, e) -> e is Element.Target } > 0 }

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
                Blender.fromJson(json["blender_old"]),
                Fragmenter.fromJson(json["fragmenter"])
        )
        */

        private data class Connection(
                val source: Id,
                val destination: Id,
                val grade: Grade = Grade.FALSE,
                val connector: Option<Element.Connector> = None
        ) {
            fun connect(graph: FscGraph): FscGraph {
                val s = graph[source]
                val d = graph[destination]
                val c = connector.orNull() ?: return graph
                return when {
                    s is Element.Connector && d is Element.Connector ->
                        graph.connect(source, destination) { c.withId() }
                    s is Element.Connector && d is Element.Target ->
                        graph.updateValue(source, c).insert(insertedEdges = setOf(FscGraph.Edge(source, destination)))
                    s is Element.Target && d is Element.Connector ->
                        graph.updateValue(destination, c).insert(insertedEdges = setOf(FscGraph.Edge(source, destination)))
                    else -> graph
                }
            }
        }

        private fun evaluateConnection(
                srcId: Id,
                destId: Id,
                graph: FscGraph
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
                        src is Element.Target && dest is Element.Target ||
                        // src is complete connector and dest is fragment
                        src is Element.Connector && src.back is None && dest is Element.Target ||
                        // src is fragment and dest is complete connector
                        src is Element.Target && dest is Element.Connector && dest.front is None
                -> default
                // src is last connector and dest is first connector
                src is Element.Connector && dest is Element.Connector -> default.copy(
                        grade = src.body.isPossible(dest.body),
                        connector = Some(Element.Connector(src.body.middle(dest.body), src.front, dest.back))
                )
                // src is incomplete connector and dest is fragment
                src is Element.Connector && src.back is Some && dest is Element.Target -> default.copy(
                        grade = src.back.value.isPossible(dest.front),
                        connector = Some(src.copy(back = Some(dest.front)))
                )
                // src is fragment and dest is incomplete connector
                src is Element.Target && dest is Element.Connector && dest.front is Some -> default.copy(
                        grade = src.back.isPossible(dest.front.value),
                        connector = Some(dest.copy(front = Some(src.back)))
                )
                else -> error("")
            }
        }
    }
}
