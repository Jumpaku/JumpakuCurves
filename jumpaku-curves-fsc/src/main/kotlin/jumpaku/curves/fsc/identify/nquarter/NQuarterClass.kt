package jumpaku.curves.fsc.identify.nquarter

enum class NQuarterClass {
    Quarter1,
    Quarter2,
    Quarter3,
    General,;

    val isGeneral : Boolean get() = this == General

    val isNQuarter: Boolean get() = !isGeneral
}