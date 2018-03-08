package jumpaku.fsc.snap.conicsection

enum class FeatureType {

    Begin, End, Center, Diameter0, Diameter1, Diameter2, Diameter3, Extra0, Extra1, Extra2, Extra3;

    val isDiameter: Boolean get() = this in listOf(Diameter0, Diameter1, Diameter2, Diameter3, Center)

    val isExtra: Boolean get() = this in listOf(Extra0, Extra1, Extra2, Extra3, Center)

    val isBeginEnd: Boolean get() = this == Begin || this == End
}