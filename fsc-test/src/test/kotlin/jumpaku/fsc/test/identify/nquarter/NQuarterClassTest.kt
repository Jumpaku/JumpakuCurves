package jumpaku.fsc.test.identify.nquarter

import jumpaku.fsc.identify.nquarter.NQuarterClass
import org.junit.Assert.*
import org.junit.Test


class NQuarterClassTest {

    @Test
    fun testIsGeneral() {
        println("IsGeneral")
        assertFalse(NQuarterClass.Quarter1.isGeneral)
        assertFalse(NQuarterClass.Quarter2.isGeneral)
        assertFalse(NQuarterClass.Quarter3.isGeneral)
        assertTrue(NQuarterClass.General.isGeneral)
    }

    @Test
    fun testIsNQuarter() {
        println("IsNQuarter")
        assertTrue(NQuarterClass.Quarter1.isNQuarter)
        assertTrue(NQuarterClass.Quarter2.isNQuarter)
        assertTrue(NQuarterClass.Quarter3.isNQuarter)
        assertFalse(NQuarterClass.General.isNQuarter)
    }
}