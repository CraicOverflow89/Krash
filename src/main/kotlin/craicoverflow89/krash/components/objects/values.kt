package craicoverflow89.krash.components.objects

import craicoverflow89.krash.components.KrashException
import craicoverflow89.krash.components.KrashMethod
import craicoverflow89.krash.components.KrashReserved
import craicoverflow89.krash.components.KrashRuntime
import kotlin.math.floor

interface KrashValue {

    fun resolve(runtime: KrashRuntime): KrashValue

    fun toSimple(runtime: KrashRuntime): KrashValueSimple {
        var result: KrashValue = resolve(runtime)
        while(result !is KrashValueSimple) result = result.toSimple(runtime)
        return result
    }

}

class KrashValueArray(private val valueList: ArrayList<KrashValue>): KrashValueSimple(hashMapOf(
    Pair("add", KrashValueCallable {_: KrashRuntime, argumentList: List<KrashValue> ->
        valueList.add(argumentList[0])
        KrashValueNull()
    }),
    Pair("join", KrashValueCallable {_: KrashRuntime, _: List<KrashValue> ->
        KrashValueString(valueList.joinToString(""))
        // NOTE: need to parse arguments as separator, prefix and postfix
    }),
    Pair("size", KrashValueInteger(valueList.size))
)) {

    init {

        // Append Members
        memberPut("each") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Validate Logic
            val logic: KrashValueCallable = argumentList[0].toSimple(runtime).let {
                if(it !is KrashValueCallable) throw KrashException("Logic must be callable!")
                it
            }

            // Invoke Logic
            valueList.forEach {
                logic.invoke(runtime, listOf(it))
            }

            // Return Array
            this
        }
    }

    fun getElement(pos: Int): KrashValue {

        // Return Element
        return if(pos < valueList.size) valueList[pos]

        // Invalid Index
        else throw KrashException("Element index $pos out of bounds for array length ${valueList.size}!")
    }

    override fun toSimple(runtime: KrashRuntime): KrashValueArray {
        return KrashValueArray(ArrayList<KrashValue>().apply {
            valueList.forEach {
                add(it.toSimple(runtime))
            }
        })
    }

    override fun toString() = valueList.joinToString(", ", "[", "]") {
        it.toString()
    }

}

class KrashValueBoolean(private val value: Boolean): KrashValueSimple() {

    override fun toString() = if(value) "true" else "false"

}

open class KrashValueCallable(private val logic: (runtime: KrashRuntime, argumentList: List<KrashValue>) -> KrashValue): KrashValueSimple() {

    fun invoke(runtime: KrashRuntime, argumentList: List<KrashValue>) = logic(runtime, argumentList)

    override fun toString() = "<callable>"

}

class KrashValueDouble(private val value: Double): KrashValueSimpleNumeric() {

    override fun toDouble() = value

    override fun toString() = value.toString()

}

class KrashValueInteger(val value: Int): KrashValueSimpleNumeric() {

    override fun toDouble() = value.toDouble()

    override fun toString() = value.toString()

}

class KrashValueMap(valueList: List<KrashValueMapPair>): KrashValueSimple() {

    // Create Data
    private val data = HashMap<String, KrashValue>().apply {
        valueList.forEach {

            // Reserved Term
            if(KrashReserved.contains(it.key)) throw KrashException("Cannot use reserved term '${it.key}' for map key!")

            // Append Pair
            put(it.key, it.value)
        }
    }

    init {

        // Append Members
        memberPut("add") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Resolve Key
            val key = argumentList[0].toSimple(runtime)

            // Append Pair
            if(key is KrashValueString) data[key.value] = argumentList[1]

            // Invalid Type
            else throw KrashException("Invalid type for map key!")

            // Done
            KrashValueNull()
        }
        /*memberPut("contains") {runtime: KrashRuntime, argumentList: List<KrashValue> ->
            KrashValueBoolean(data.containsKey(argumentList[0].toSimple(runtime)))
        }*/
        // NOTE: need to get the above working
        memberPut("keys", KrashValueArray(ArrayList<KrashValue>().apply {
            data.forEach {
                add(KrashValueString(it.key))
            }
        }))
    }

    fun getData() = data

    fun getData(key: String): KrashValue {

        // Return Element
        return if(data.containsKey(key)) data[key]!!

        // Invalid Index
        else throw KrashException("Invalid key '$key' for map!")
    }

    override fun toSimple(runtime: KrashRuntime) =
        KrashValueMap(data.map {
            KrashValueMapPair(it.key, it.value.toSimple(runtime))
        })

    override fun toString() = ArrayList<String>().apply {
        data.forEach {
            add("${it.key}: ${it.value}")
        }
    }.joinToString(", ", "{", "}")

}

class KrashValueMapPair(val key: String, val value: KrashValue) {

    override fun toString() = "$key: $value"

}

class KrashValueNull: KrashValueSimple() {

    override fun toString() = "null"

}

class KrashValueObject(private val obj: KrashValueClass, memberList: HashMap<String, KrashValue>): KrashValueSimple(memberList) {

    override fun toString(): String {

        // Invoke Member
        if(memberContains("toString")) memberGet("toString").let {

            // Custom String
            if(it is KrashValueString) return@toString it.value
        }

        // Default Value
        return "<object ${obj.name}>"
    }

}

abstract class KrashValueSimple(private val memberList: HashMap<String, KrashValue> = hashMapOf()): KrashValue {

    fun memberContains(key: String) = memberList.containsKey(key)

    fun memberGet(key: String): KrashValue = memberList[key] ?: throw KrashException("No member '$key' exists on value!")

    fun memberPut(key: String, value: KrashValue) {
        memberList[key] = value
    }

    fun memberPut(key: String, value: (runtime: KrashRuntime, argumentList: List<KrashValue>) -> KrashValue) {
        memberList[key] = KrashValueCallable(value)
    }

    override fun resolve(runtime: KrashRuntime) = this

}

abstract class KrashValueSimpleNumeric: KrashValueSimple() {

    companion object {

        fun create(value: Double): KrashValueSimpleNumeric {

            // Create Integer
            if(value == floor(value) && !value.isInfinite()) return KrashValueInteger(value.toInt())

            // Create Double
            return KrashValueDouble(value)
        }

    }

    abstract fun toDouble(): Double

}

class KrashValueString(val value: String): KrashValueSimple(hashMapOf(
    Pair("size", KrashValueInteger(value.length)),
    Pair("toList",
        KrashValueCallable {_: KrashRuntime, _: List<KrashValue> ->
            KrashValueArray(ArrayList<KrashValue>().apply {
                var pos = 0
                while (pos < value.length) {
                    add(KrashValueString(value.substring(pos, pos + 1)))
                    pos++
                }
            })
        })
)) {

    fun getChar(pos: Int) = KrashValueString(pos.let {

        // Negative Position
        if (it < 0) value.length + it

        // Positive Position
        else it
    }.let { pos ->

        // Invalid Position
        if (pos >= value.length || pos < 0) throw KrashException("Character index $pos out of bounds for string length ${value.length}!")

        // Fetch Character
        value.substring(pos, pos + 1)
        // NOTE: need to consider complex positions like [1, 3] (char 1 to 3)
    })

    override fun toString() = value

}

class KrashValueReference(val value: String, val byRef: Boolean): KrashValue {

    override fun resolve(runtime: KrashRuntime): KrashValue {

        // Native Method
        if(KrashMethod.nativeContains(value)) return KrashMethod.nativeGet(value)

        // Native Object
        if(KrashValueClass.nativeContains(value)) return KrashValueClass.nativeGet(value)

        // Custom Reference
        return runtime.heapGet(value)
    }

    override fun toString() = value

}