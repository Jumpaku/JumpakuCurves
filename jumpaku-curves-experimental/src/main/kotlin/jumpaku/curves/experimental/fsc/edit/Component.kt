package jumpaku.curves.experimental.fsc.edit


import jumpaku.commons.control.None
import jumpaku.commons.control.Option
import jumpaku.commons.control.Some
import jumpaku.commons.control.optionWhen


open class Component<K : Any, V : Any>(elements: Map<K, OrderedElement<V>>, val isClosed: Boolean)
    : Graph<K, V>(constructGraph(elements, isClosed)) {

    class OrderedElement<V>(val order: Int, val element: V)

    private val orderedVertices: List<K> = elements.toList().sortedBy { it.second.order }.map { it.first }

    val first: Option<Pair<K, V>> = optionWhen(!isClosed) {
        orderedVertices.first() to getValue(orderedVertices.first())
    }

    val last: Option<Pair<K, V>> = optionWhen(!isClosed) {
        orderedVertices.last() to getValue(orderedVertices.last())
    }

    companion object {

        fun <K : Any, V : Any> constructGraph(
                elements: Map<K, OrderedElement<V>>,
                isClosed: Boolean
        ): Map<K, Vertex<K, V>> {
            val g = elements.mapValues { Vertex<K, V>(it.value.element, None, None) }.toMutableMap()
            val l = elements.entries.sortedBy { it.value.order }
            (l + if (isClosed) listOf(l.first()) else listOf())
                    .zipWithNext { e0, e1 -> Edge(e0.key, e1.key) }
                    .forEach { e ->
                        g.compute(e.source) { _, v -> v!!.copy(outgoing = Some(e)) }
                        g.compute(e.destination) { _, v -> v!!.copy(incoming = Some(e)) }
                    }
            return g
        }
    }
}
