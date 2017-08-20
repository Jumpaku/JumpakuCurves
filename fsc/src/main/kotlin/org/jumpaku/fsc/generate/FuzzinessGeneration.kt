package org.jumpaku.fsc.generate

import org.jumpaku.core.affine.Vector

fun generateFuzziness(velocity: Vector, acceleration: Vector): Double {
    val velocityCoefficient = 0.004
    val accelerationCoefficient = 0.003
    return velocityCoefficient*velocity.length() + accelerationCoefficient*acceleration.length()
}