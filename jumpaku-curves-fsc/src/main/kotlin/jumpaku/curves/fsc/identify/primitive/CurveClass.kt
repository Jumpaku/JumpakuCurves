package jumpaku.curves.fsc.identify.primitive


enum class CurveClass {
    Point,
    LineSegment,
    Circle,
    CircularArc,
    Ellipse,
    EllipticArc,
    ClosedFreeCurve,
    OpenFreeCurve, ;

    val isFreeCurve: Boolean get() = this == ClosedFreeCurve || this == OpenFreeCurve
    val isConicSection: Boolean get() = !isFreeCurve
    val isOpen: Boolean get() = this == LineSegment || this == CircularArc || this == EllipticArc || this == OpenFreeCurve
    val isClosed: Boolean get() = !isOpen
    val isElliptic: Boolean get() = this == Ellipse || this == EllipticArc
    val isCircular: Boolean get() = this == Circle || this == CircularArc
    val isLinear: Boolean get() = this == Point || this == LineSegment
}