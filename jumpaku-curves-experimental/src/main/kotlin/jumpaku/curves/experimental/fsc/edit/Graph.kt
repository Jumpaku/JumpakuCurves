package jumpaku.curves.experimental.fsc.edit


import jumpaku.commons.control.*
import java.util.stream.Stream
import kotlin.streams.toList


open class Graph<K : Any, V : Any>(structure: Map<K, Vertex<K, V>> = emptyMap())
    : Map<K, V> by structure.mapValues({ it.value.element }) {

    data class Edge<K : Any>(val source: K, val destination: K)

    data class Vertex<K : Any, V : Any>(
            val element: V,
            val incoming: Option<Edge<K>> = None,
            val outgoing: Option<Edge<K>> = None
    ) {

        constructor(element: V, incoming: Edge<K>, outgoing: Edge<K>) : this(element, Some(incoming), Some(outgoing))
    }

    private val structure: Map<K, Vertex<K, V>> = structure.toMap()

    protected val outgoingEdges: Map<K, Option<Edge<K>>> = structure.mapValues { it.value.outgoing }

    protected val incomingEdges: Map<K, Option<Edge<K>>> = structure.mapValues { it.value.incoming }

    val vertices: Set<K> = structure.keys

    val edges: Set<Edge<K>> = (outgoingEdges.values + incomingEdges.values).flatten().toSet()

    fun compose(g: Graph<K, V>): Graph<K, V> = Graph(structure + g.structure)

    fun nextOf(v: K): Option<K> {
        require(v in keys) { "$v is not in $keys" }
        return outgoingEdges[v]!!.map { it.destination }
    }

    fun prevOf(v: K): Option<K> {
        require(v in keys) { "$v is not in $keys" }
        return incomingEdges[v]!!.map { it.source }
    }

    fun remove(removedIds: Iterable<K> = emptySet(), removedEdges: Iterable<Edge<K>> = emptySet()): Graph<K, V> {
        val g = structure.toMutableMap()
        g.filterValues { it.incoming is Some && it.incoming.value.source in removedIds }
                .forEach { k, v -> g.replace(k, v.copy(incoming = None)) }
        g.filterValues { it.outgoing is Some && it.outgoing.value.destination in removedIds }
                .forEach { k, v -> g.replace(k, v.copy(outgoing = None)) }
        g.filterValues { it.incoming is Some && it.incoming.value in removedEdges }
                .forEach { k, v -> g.replace(k, v.copy(incoming = None)) }
        g.filterValues { it.outgoing is Some && it.outgoing.value in removedEdges }
                .forEach { k, v -> g.replace(k, v.copy(outgoing = None)) }
        removedIds.forEach { g.remove(it) }
        return Graph(g)
    }

    fun insert(insertedElements: Map<K, V> = emptyMap(), insertedEdges: Iterable<Edge<K>> = emptySet()): Graph<K, V> {
        val g = structure.toMutableMap()
        insertedElements.forEach { (id, e) -> g.compute(id) { _, v -> v?.copy(element = e) ?: Vertex(e, None, None) } }
        check(insertedEdges.all { it.source in g && it.destination in g })
        check(insertedEdges.intersect(g.flatMap { it.value.incoming + it.value.outgoing }).none())
        insertedEdges.forEach { e ->
            g.replace(e.source, g.getValue(e.source).copy(outgoing = Some(e)))
            g.replace(e.destination, g.getValue(e.destination).copy(incoming = Some(e)))
        }
        return Graph(g)
    }

    fun updateValue(id: K, value: V): Graph<K, V> {
        require(id in this)
        val g = structure.toMutableMap()
        g.compute(id) { _, v -> v!!.copy(element = value) }
        return Graph(g)
    }

    fun connect(front: K, back: K, f: () -> Pair<K, V>): Graph<K, V> {
        require(front in this)
        require(back in this)
        val g = structure.toMutableMap()
        g.filterValues { it.incoming is Some && it.incoming.value.destination == back }
        val (k, v) = f()
        g[k] = Vertex(v)
        incomingEdges.getValue(front).map { it.copy(destination = k) }.forEach { e ->
            g.replace(e.source, g.getValue(e.source).copy(outgoing = Some(e)))
            g.replace(k, g.getValue(k).copy(incoming = Some(e)))
        }
        outgoingEdges.getValue(back).map { it.copy(source = k) }.forEach { e ->
            g.replace(e.destination, g.getValue(e.destination).copy(incoming = Some(e)))
            g.replace(k, g.getValue(k).copy(outgoing = Some(e)))
        }
        g.remove(front)
        g.remove(back)
        return Graph(g)
    }

    fun decompose(): List<Component<K, V>> {
        val components = mutableListOf<Component<K, V>>()
        val vs = keys.toMutableSet()
        while (vs.isNotEmpty()) {
            val origin = vs.first()
            val front = Stream.iterate(some(origin)) {
                it.flatMap { cur -> prevOf(cur).filter { prev -> prev != origin } }
            }.takeWhile { it.isDefined }.toList().flatten().drop(1).reversed()
            val back = Stream.iterate(some(origin)) {
                it.flatMap { cur -> nextOf(cur).filter { next -> next != origin } }
            }.takeWhile { it.isDefined }.toList().flatten().drop(1)
            val component = Component(
                    ((front + origin + (back - front)).mapIndexed { index, it ->
                        it to Component.OrderedElement(
                                index,
                                getValue(it)
                        )
                    }.toMap()),
                    (nextOf(origin) + prevOf(origin)).isNotEmpty() && front == back
            )
            vs -= component.keys
            components += component
        }
        return components
    }

    companion object {

        fun <K : Any, V : Any> compose(gs: List<Graph<K, V>>): Graph<K, V> = gs.fold(Graph(), Graph<K, V>::compose)

        fun <K : Any> vertex(v: K, incoming: Option<K> = None, outgoing: Option<K> = None): Vertex<K, K> =
                Vertex(v, incoming.map { Edge(it, v) }, outgoing.map { Edge(v, it) })

        fun <K : Any> of(structure: Set<Vertex<K, K>>): Graph<K, K> = Graph(structure.map { it.element to it }.toMap())
    }
}