package jumpaku.curves.fsc.experimental.edit


import jumpaku.commons.control.None
import jumpaku.commons.control.Option
import jumpaku.commons.control.Some
import jumpaku.commons.control.optionWhen
import jumpaku.curves.core.curve.bspline.BSpline
import jumpaku.curves.core.geom.Point


open class FscPath internal constructor(elements: Map<Id, OrderedElement>, val isClosed: Boolean)
    : FscGraph(constructGraph(elements, isClosed)) {

    internal class OrderedElement(val order: Int, val element: Element)

    private val orderedVertices: List<Id> = elements.toList().sortedBy { it.second.order }.map { it.first }

    val first: Option<Pair<Id, Element>> = optionWhen(!isClosed) {
        orderedVertices.first() to getValue(orderedVertices.first())
    }

    val last: Option<Pair<Id, Element>> = optionWhen(!isClosed) {
        orderedVertices.last() to getValue(orderedVertices.last())
    }

    init {
        orderedVertices.zipWithNext().forEach { (prevId, nextId) ->
            val prev = getValue(prevId)
            val next = getValue(nextId)
            require((prev is Element.Connector && next is Element.Target && prev.back is Some) ||
                    (prev is Element.Target && next is Element.Connector && next.front is Some))
        }
        (getValue(orderedVertices.first()) as? Element.Connector)?.run { require(front is None || !isClosed) }
        (getValue(orderedVertices.last()) as? Element.Connector)?.run { require(back is None || !isClosed) }
    }

    fun connectors(): List<Element.Connector> = mapNotNull { it.value as? Element.Connector }

    fun fragments(): List<Element.Target> = mapNotNull { it.value as? Element.Target }

    data class FragmentWithConnectors(
            val fragment: BSpline,
            val front: Option<Point>,
            val back: Option<Point>
    )

    fun fragmentsWithConnectors(): List<FragmentWithConnectors> =
            filterValues { it is Element.Target }
                    .map { (id, element) ->
                        val f = element as Element.Target
                        val (front, back) = listOf(prevOf(id), nextOf(id)).map {
                            it.map { (getValue(it) as Element.Connector).body }
                        }
                        FragmentWithConnectors(f.fragment, front, back)
                    }

    companion object {

        fun openPath(orderedElements: List<Pair<Id, Element>>): FscPath = FscPath(
                orderedElements.mapIndexed { i, (id, e) ->
                    id to OrderedElement(i, e)
                }.toMap(), false)

        private fun constructGraph(
                elements: Map<Id, OrderedElement>,
                isClosed: Boolean
        ): Map<Id, Vertex> {
            val g = elements.mapValues { Vertex(it.value.element, None, None) }.toMutableMap()
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
