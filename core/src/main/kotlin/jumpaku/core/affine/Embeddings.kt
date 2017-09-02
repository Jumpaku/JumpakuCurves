package jumpaku.core.affine

import io.vavr.control.Option
import io.vavr.control.Try
import org.apache.commons.math3.geometry.euclidean.threed.Line
import org.apache.commons.math3.geometry.euclidean.threed.Plane
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

fun line(p0: Point, p1: Point): Option<Line> = Try.ofSupplier {
    Line(Vector3D(p0.x, p0.y, p0.z), Vector3D(p1.x, p1.y, p1.z), 1.0e-10)
}.toOption()

fun line(p: Point, d: Vector): Option<Line> = line(p, p + d)

fun plane(p: Point, n: Vector): Option<Plane> = Try.ofSupplier {
    Plane(Vector3D(p.x, p.y, p.z), Vector3D(n.x, n.y, n.z), 1.0e-10)
}.toOption()

fun plane(p0: Point, p1: Point, p2: Point): Option<Plane> = plane(p0, (p1 - p0).cross(p2 - p0))
