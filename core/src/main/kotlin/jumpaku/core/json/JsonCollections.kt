package jumpaku.core.json

import io.vavr.Tuple2
import io.vavr.collection.Array
import io.vavr.collection.HashMap
import io.vavr.collection.Map
import io.vavr.control.Option



data class OptionJson<TJ>(val value: TJ? = null) {
    fun option(): Option<TJ> = Option.`when`(value != null) { value!! }
    fun <T> option(fromJson: (TJ)->T): Option<T> = Option.`when`(value != null) { fromJson(value!!) }
}

fun <TJ> Option<TJ>.json(): OptionJson<TJ> = if(isDefined) OptionJson(get()) else OptionJson()
fun <T, TJ> Option<T>.json(toJson: (T) -> TJ): OptionJson<TJ> = OptionJson(if (isDefined) toJson(get()) else null)



fun <TJ> Array<TJ>.json(): List<TJ> = toJavaList()
fun <T, TJ> Array<T>.json(toJson: (T)->TJ): List<TJ> = map(toJson).toJavaList()
fun <TJ> List<TJ>.array(): Array<TJ> = Array.ofAll(this)
fun <TJ, T> List<TJ>.array(fromJson: (TJ)->T): Array<T> = Array.ofAll(map(fromJson))



data class KeyValueJson<KJ, VJ>(val key: KJ, val value: VJ) {
    fun <K, V> tuple2(fromJson: (KJ, VJ)->Tuple2<K, V>): Tuple2<K, V> = fromJson(key, value)
    fun <K> tuple2Key(keyFromJson: (KJ)->K): Tuple2<K, VJ> = tuple2 { kj, vj -> Tuple2(keyFromJson(kj), vj) }
    fun <V> tuple2Value(valueFromJson: (VJ)->V): Tuple2<KJ, V> = tuple2 { kj, vj -> Tuple2(kj, valueFromJson(vj)) }
    fun <K, V> tuple2(keyFromJson: (KJ)->K, valueFromJson: (VJ)->V): Tuple2<K, V> {
        return tuple2 { kj, vj -> Tuple2(keyFromJson(kj), valueFromJson(vj)) }
    }
}

fun <KJ, VJ> Tuple2<KJ, VJ>.json(): KeyValueJson<KJ, VJ> = KeyValueJson(_1, _2)
fun <K, KJ, VJ> Tuple2<K, VJ>.json1(keyToJson: (K)->KJ): KeyValueJson<KJ, VJ> = json(keyToJson, { it })
fun <KJ, V, VJ> Tuple2<KJ, V>.json2(valueToJson: (V)->VJ): KeyValueJson<KJ, VJ> = json({ it }, valueToJson)
fun <K, V, KJ, VJ> Tuple2<K, V>.json(keyToJson: (K)->KJ, valueToJson: (V)->VJ): KeyValueJson<KJ, VJ> {
    return KeyValueJson(keyToJson(_1), valueToJson(_2))
}



fun <KJ, VJ> Map<KJ, VJ>.json(): List<KeyValueJson<KJ, VJ>> = json({ it }, { it })
fun <K, KJ, VJ> Map<K, VJ>.jsonKey(keyToJson: (K)->KJ): List<KeyValueJson<KJ, VJ>> = json(keyToJson, { it })
fun <V, KJ, VJ> Map<KJ, V>.jsonValue(valueToJson: (V)->VJ): List<KeyValueJson<KJ, VJ>> = json({ it }, valueToJson)
fun <K, V, KJ, VJ> Map<K, V>.json(keyToJson: (K)->KJ, valueToJson: (V)->VJ): List<KeyValueJson<KJ, VJ>> {
    return toJavaList().map { it.json(keyToJson, valueToJson) }
}

fun <KJ, VJ> List<KeyValueJson<KJ, VJ>>.hashMap(): Map<KJ, VJ> = hashMap({ it }, { it })
fun <KJ, VJ, K> List<KeyValueJson<KJ, VJ>>.hashMapKey(keyFromJson: (KJ)->K): Map<K, VJ> = hashMap(keyFromJson, { it })
fun <KJ, VJ, V> List<KeyValueJson<KJ, VJ>>.hashMapValue(valueFromJson: (VJ) -> V): Map<KJ, V> = hashMap({ it }, valueFromJson)
fun <KJ, VJ, K, V> List<KeyValueJson<KJ, VJ>>.hashMap(keyFromJson: (KJ)->K, valueFromJson: (VJ) -> V): Map<K, V> {
    return HashMap.ofEntries(map { it.tuple2(keyFromJson, valueFromJson) })
}


