package org.jumpaku.core.fsci

import org.jumpaku.core.affine.Vector

fun generateFuzziness(velocity: Vector, acceleration: Vector): Double {
    val velocityCoefficient: Double = 0.004
    val accelerationCoefficient: Double = 0.003
    return velocityCoefficient*velocity.length() + accelerationCoefficient*acceleration.length()
}