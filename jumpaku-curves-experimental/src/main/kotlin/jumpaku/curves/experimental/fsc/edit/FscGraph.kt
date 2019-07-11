package jumpaku.curves.experimental.fsc.edit


import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.github.salomonbrys.kotson.toJson
import com.google.gson.JsonElement
import jumpaku.commons.control.*
import jumpaku.commons.json.ToJson
import jumpaku.commons.json.jsonMap
import jumpaku.commons.json.map
import java.util.stream.Stream
import kotlin.streams.toList


data class Id(val elementId: String)

open class FscGraph protected constructor(private val structure: Map<Id, Vertex> = emptyMap())
    : Map<Id, Element> by (structure.mapValues { (_, v) -> v.element }), ToJson {

    constructor(elements: Map<Id, Element>,
                outgoing: Map<Id, Option<Id>>,
                incoming: Map<Id, Option<Id>>) : this(elements.map { (id, e) ->
        id to Vertex(
                element = e,
                incoming = incoming.getValue(id).map { Edge(it, id) },
                outgoing = outgoing.getValue(id).map { Edge(id, it) })
    }.toMap())

    data class Edge(val source: Id, val destination: Id)

    data class Vertex(
            val element: Element,
            val incoming: Option<Edge> = None,
            val outgoing: Option<Edge> = None)

    val vertices: Set<Id> = structure.keys

    private val outgoing: Map<Id, Option<Id>> = structure.mapValues { it.value.outgoing.map { it.destination } }

    private val incoming: Map<Id, Option<Id>> = structure.mapValues { it.value.incoming.map { it.source } }

    val edges: Set<Edge> = structure.flatMap { (_, v) -> v.incoming + v.outgoing }.toSet()

    fun compose(g: FscGraph): FscGraph = FscGraph(structure + g.structure)

    fun nextOf(v: Id): Option<Id> {
        require(v in keys) { "$v is not in $keys" }
        return outgoing[v]!!//.map { it.destination }
    }

    fun prevOf(v: Id): Option<Id> {
        require(v in keys) { "$v is not in $keys" }
        return incoming[v]!!//.map { it.source }
    }

    fun remove(removedIds: Iterable<Id> = emptySet(), removedEdges: Iterable<Edge> = emptySet()): FscGraph {
        val g = structure.toMutableMap()
        g.filterValues { it.incoming is Some && it.incoming.value.source in removedIds }
                .forEach { (k, v) -> g.replace(k, v.copy(incoming = None)) }
        g.filterValues { it.outgoing is Some && it.outgoing.value.destination in removedIds }
                .forEach { (k, v) -> g.replace(k, v.copy(outgoing = None)) }
        g.filterValues { it.incoming is Some && it.incoming.value in removedEdges }
                .forEach { (k, v) -> g.replace(k, v.copy(incoming = None)) }
        g.filterValues { it.outgoing is Some && it.outgoing.value in removedEdges }
                .forEach { (k, v) -> g.replace(k, v.copy(outgoing = None)) }
        removedIds.forEach { g.remove(it) }
        return FscGraph(g)
    }

    fun insert(insertedElements: Map<Id, Element> = emptyMap(), insertedEdges: Iterable<Edge> = emptySet()): FscGraph {
        val g = structure.toMutableMap()
        insertedElements.forEach { (id, e) -> g.compute(id) { _, v -> v?.copy(element = e) ?: Vertex(e, None, None) } }
        check(insertedEdges.all { it.source in g && it.destination in g })
        check(insertedEdges.intersect(g.flatMap { it.value.incoming + it.value.outgoing }).none())
        insertedEdges.forEach { e ->
            g.replace(e.source, g.getValue(e.source).copy(outgoing = Some(e)))
            g.replace(e.destination, g.getValue(e.destination).copy(incoming = Some(e)))
        }
        return FscGraph(g)
    }

    fun updateValue(id: Id, value: Element): FscGraph {
        require(id in this)
        val g = structure.toMutableMap()
        g.compute(id) { _, v -> v!!.copy(element = value) }
        return FscGraph(g)
    }

    fun connect(front: Id, back: Id, f: () -> Pair<Id, Element>): FscGraph {
        require(front in this)
        require(back in this)
        val g = structure.toMutableMap()
        g.filterValues { it.incoming is Some && it.incoming.value.destination == back }
        val (k, v) = f()
        g[k] = Vertex(v)
        prevOf(front).forEach { prev ->
            val e = Edge(prev, k)
            g.replace(prev, g.getValue(prev).copy(outgoing = Some(e)))
            g.replace(k, g.getValue(k).copy(incoming = Some(e)))
        }
        nextOf(back).forEach { next ->
            val e = Edge(k, next)
            g.replace(next, g.getValue(next).copy(incoming = Some(e)))
            g.replace(k, g.getValue(k).copy(outgoing = Some(e)))
        }
        g.remove(front)
        g.remove(back)
        return FscGraph(g)
    }

    fun decompose(): List<FscPath> {
        val components = mutableListOf<FscPath>()
        val vs = keys.toMutableSet()
        while (vs.isNotEmpty()) {
            val origin = vs.first()
            val front = Stream.iterate(some(origin)) {
                it.flatMap { cur -> prevOf(cur).filter { prev -> prev != origin } }
            }.takeWhile { it.isDefined }.toList().flatten().drop(1).reversed()
            val back = Stream.iterate(some(origin)) {
                it.flatMap { cur -> nextOf(cur).filter { next -> next != origin } }
            }.takeWhile { it.isDefined }.toList().flatten().drop(1)
            val component = FscPath(
                    ((front + origin + (back - front)).mapIndexed { index, it ->
                        it to FscPath.OrderedElement(index, getValue(it))
                    }.toMap()),
                    (nextOf(origin) + prevOf(origin)).isNotEmpty() && front == back
            )
            vs -= component.keys
            components += component
        }
        return components
    }

    override fun toJson(): JsonElement = jsonObject(
            "elements" to jsonMap(map { (k, v) ->
                k.elementId.toJson() to v.toJson()
            }.toMap()),
            "outgoing" to jsonMap(outgoing.map { (k, v) ->
                k.elementId.toJson() to v.map { it.elementId.toJson() }.toJson()
            }.toMap()),
            "incoming" to jsonMap(incoming.map { (k, v) ->
                k.elementId.toJson() to v.map { it.elementId.toJson() }.toJson()
            }.toMap()))

    override fun toString(): String = toJsonString()

    companion object {

        fun compose(gs: List<FscGraph>): FscGraph = gs.fold(FscGraph(), FscGraph::compose)

        fun fromJson(json: JsonElement): FscGraph = FscGraph(
                json["elements"].map.map { (k, v) -> Id(k.string) to Element.fromJson(v) }.toMap(),
                json["outgoing"].map.map { (k, v) -> Id(k.string) to Option.fromJson(v).map { Id(it.string) } }.toMap(),
                json["incoming"].map.map { (k, v) -> Id(k.string) to Option.fromJson(v).map { Id(it.string) } }.toMap()
        )
    }
}