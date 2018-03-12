package jumpaku.fsc.snap.conicsection

import io.vavr.collection.Stream
import jumpaku.core.affine.Point
import jumpaku.core.curve.rationalbezier.ConicSection

data class LinearFeaturePoints(val featurePoint0: Point, val featurePoint1: Point)

data class CircularFeaturePoints(val featurePoint0: Point, val featurePoint1: Point, val forNormal: Point)

data class EllipticFeaturePoints(val featurePoint0: Point, val featurePoint1: Point, val featurePoint2: Point)

interface FeaturePointsCombinator {

    fun linearCombinations(conicSection: ConicSection, isOpen: Boolean): Stream<LinearFeaturePoints>

    fun circularCombinations(conicSection: ConicSection, isOpen: Boolean): Stream<CircularFeaturePoints>

    fun ellipticCombinations(conicSection: ConicSection, isOpen: Boolean): Stream<EllipticFeaturePoints>
}