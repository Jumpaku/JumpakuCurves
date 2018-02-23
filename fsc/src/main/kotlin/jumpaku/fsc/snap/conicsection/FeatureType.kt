package jumpaku.fsc.snap.conicsection

enum class FeatureType {

    Begin, End, Center, Diameter0, Diameter1, Diameter2, Diameter3, Extra0, Extra1, Extra2, Extra3;

    val isDiameter: Boolean get() = this == Diameter0 || this == Diameter1 || this == Diameter2 || this == Diameter3

    val isExtra: Boolean get() = this == Extra0 || this == Extra1 || this == Extra2 || this == Extra3

    val isBeginEnd: Boolean get() = this == Begin || this == End
}