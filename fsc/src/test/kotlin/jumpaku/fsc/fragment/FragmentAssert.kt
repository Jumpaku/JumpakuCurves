package jumpaku.fsc.fragment

import jumpaku.core.curve.intervalAssertThat
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

fun fragmentAssertThat(actual: Fragment): FragmentAssert = FragmentAssert(actual)

class FragmentAssert(actual: Fragment) : AbstractAssert<FragmentAssert, Fragment>(actual, FragmentAssert::class.java) {

    fun isEqualToFragment(expected: Fragment, eps: Double = 1.0e-10): FragmentAssert {
        isNotNull
        intervalAssertThat(actual.interval).`as`("interval of fragment").isEqualToInterval(expected.interval, eps)
        Assertions.assertThat(actual.type).`as`("type of fragment").isEqualTo(expected.type)
        return this
    }
}