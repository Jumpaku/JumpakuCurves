package jumpaku.curves.fsc.snap

import jumpaku.curves.core.geom.Point
import jumpaku.curves.core.geom.Vector
import jumpaku.curves.core.transform.*
import org.apache.commons.math3.util.FastMath

class Grid(
    val baseSpacingInWorld: Double,
    val baseFuzzinessInWorld: Double = 0.0,
    val magnification: Int = 2,
    val gridToWorld: SimilarityTransform = SimilarityTransform.Identity
) : SimilarlyTransformable<Grid> {

    constructor(
        baseSpacingInWorld: Double,
        baseFuzzinessInWorld: Double = 0.0,
        magnification: Int = 2,
        originInWorld: Point = Point(0.0, 0.0, 0.0, 0.0),
        rotationInWorld: Rotate = Rotate(Vector.K, 0.0)
    ) : this(
        baseSpacingInWorld,
        baseFuzzinessInWorld,
        magnification,
        SimilarityTransform.Identity
            .andThen(rotationInWorld)
            .andThen(UniformlyScale(baseSpacingInWorld))
            .andThen(Translate(originInWorld - Point.origin))
    )

    init {
        require(baseSpacingInWorld > 0.0)
        require(baseFuzzinessInWorld >= 0.0)
        require(magnification > 1)
    }

    fun spacingInWorld(resolution: Int): Double =
        baseSpacingInWorld * FastMath.pow(magnification.toDouble(), -resolution)

    fun fuzzinessInWorld(resolution: Int): Double =
        baseFuzzinessInWorld * FastMath.pow(magnification.toDouble(), -resolution)

    fun originInWorld(): Point = Point(gridToWorld.move())

    fun rotationInWorld(): SimilarityTransform = gridToWorld.rotation()

    /**
     * localToWorld transforms coordinates in local(grid) to coordinates in world.
     * Coordinates in world is transformed by the following transformations;
     *  scaling by spacing,
     *  translation to specified origin.
     */
    fun localToWorld(resolution: Int): SimilarityTransform = gridToWorld.rotation()
        .andThen(UniformlyScale(spacingInWorld(resolution)))
        .andThen(Translate(gridToWorld.move()))

    fun snapToNearestGrid(cursor: Point, resolution: Int): GridPoint =
        localToWorld(resolution).invert().orThrow()(cursor)
            .let { (x, y, z) -> GridPoint(FastMath.round(x), FastMath.round(y), FastMath.round(z)) }

    fun transformToWorld(gridPoint: GridPoint, resolution: Int): Point = gridPoint.run {
        localToWorld(resolution)(Point.xyz(x.toDouble(), y.toDouble(), z.toDouble()))
            .copy(r = fuzzinessInWorld(resolution))
    }

    override fun similarlyTransform(a: SimilarityTransform): Grid = Grid(
        baseSpacingInWorld = baseSpacingInWorld * a.scale(),
        baseFuzzinessInWorld = baseFuzzinessInWorld * a.scale(),
        magnification = magnification,
        gridToWorld = gridToWorld.andThen(a)
    )
}

