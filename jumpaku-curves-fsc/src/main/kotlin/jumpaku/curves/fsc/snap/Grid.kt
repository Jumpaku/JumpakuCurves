package jumpaku.curves.fsc.snap

import jumpaku.commons.control.Option
import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.transform.*
import org.apache.commons.math3.util.FastMath

class Grid(
    val baseFuzzinessInWorld: Double = 0.0,
    val magnification: Int = 2,
    val baseGridToWorld: SimilarityTransform = SimilarityTransform.Identity
) : SimilarlyTransformable<Grid> {

    constructor(
        baseSpacingInWorld: Double,
        baseFuzzinessInWorld: Double = 0.0,
        magnification: Int = 2,
        originInWorld: Point = Point(0.0, 0.0, 0.0, 0.0),
        rotationInWorld: Rotate = Rotate(Vector.K, 0.0)
    ) : this(
        baseFuzzinessInWorld,
        magnification,
        SimilarityTransform.Identity
            .andThen(rotationInWorld)
            .andThen(UniformlyScale(baseSpacingInWorld))
            .andThen(Translate(originInWorld - Point.origin))
    )

    init {
        require(baseFuzzinessInWorld >= 0.0)
        require(magnification > 1)
        require(baseGridToWorld.scale() > 0.0)
    }

    val baseSpacingInWorld: Double = baseGridToWorld.scale()

    val originInWorld: Point = Point(baseGridToWorld.move())

    val worldToBaseGrid: SimilarityTransform = baseGridToWorld.invert().orThrow()

    fun spacingInWorld(resolution: Int): Double =
        baseSpacingInWorld * FastMath.pow(magnification.toDouble(), -resolution)

    fun fuzzinessInWorld(resolution: Int): Double =
        baseFuzzinessInWorld * FastMath.pow(magnification.toDouble(), -resolution)

    /**
     * gridToWorld transforms coordinates in a grid of the specified resolution to coordinates in world.
     * Coordinates in world is transformed by the following transformations;
     *  rotation by baseGridToWorld.rotation()
     *  scaling by baseSpacingInWorld,
     *  translation to originInWorld.
     */
    fun gridToWorld(resolution: Int): SimilarityTransform = baseGridToWorld.rotation()
        .andThen(UniformlyScale(spacingInWorld(resolution)))
        .andThen(Translate(originInWorld.toVector()))

    fun snapToNearestGrid(cursor: Point, resolution: Int): GridPoint =
        gridToWorld(resolution).invert().orThrow()(cursor)
            .let { (x, y, z) -> GridPoint(FastMath.round(x), FastMath.round(y), FastMath.round(z)) }

    fun transformToWorld(gridPoint: GridPoint, resolution: Int): Point = gridPoint.run {
        gridToWorld(resolution)(Point.xyz(x.toDouble(), y.toDouble(), z.toDouble()))
            .copy(r = fuzzinessInWorld(resolution))
    }

    override fun similarlyTransform(a: SimilarityTransform): Grid = Grid(
        baseFuzzinessInWorld = baseFuzzinessInWorld * a.scale(),
        magnification = magnification,
        baseGridToWorld = baseGridToWorld.andThen(a)
    )
}

