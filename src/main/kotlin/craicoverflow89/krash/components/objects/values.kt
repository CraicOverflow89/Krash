package craicoverflow89.krash.components.objects

import craicoverflow89.krash.components.KrashMethod
import craicoverflow89.krash.components.KrashRuntime

interface KrashValue {

    fun resolve(runtime: KrashRuntime): KrashValue

    fun toSimple(runtime: KrashRuntime): KrashValueSimple {
        var result: KrashValue = resolve(runtime)
        while(result !is KrashValueSimple) result = result.toSimple(runtime)
        return result
    }

}

class KrashValueArray(val valueList: List<KrashValue>): KrashValueSimple(hashMapOf(
    Pair("join",
        KrashValueCallable { runtime: KrashRuntime, argumentList: List<KrashValue> ->
            KrashValueString(valueList.joinToString(""))
            // NOTE: need to parse arguments as separator, prefix and postfix
        }),
    Pair("size", KrashValueInteger(valueList.size))
)) {

    init {

        // Append Members
        memberPut("each") { runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Validate Logic
            val logic: KrashValueCallable = argumentList[0].toSimple(runtime).let {
                if(it !is KrashValueCallable) throw RuntimeException("Logic must be callable!")
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
        else throw RuntimeException("Element index $pos out of bounds for array length ${valueList.size}!")
    }

    override fun toSimple(runtime: KrashRuntime) =
        KrashValueArray(valueList.map {
            it.toSimple(runtime)
        })

    override fun toString() = valueList.joinToString(", ", "[", "]") {
        it.toString()
    }

}

class KrashValueBoolean(val value: Boolean): KrashValueSimple() {

    override fun toString() = if(value) "true" else "false"

}

open class KrashValueCallable(private val logic: (runtime: KrashRuntime, argumentList: List<KrashValue>) -> KrashValue): KrashValueSimple() {

    fun invoke(runtime: KrashRuntime, argumentList: List<KrashValue>) = logic(runtime, argumentList)

    override fun toString() = "<callable>"

}

class KrashValueIndex(val value: KrashValue, private val index: KrashValueIndexPos):
    KrashValue {

    override fun resolve(runtime: KrashRuntime): KrashValue = index.let{

        // Resolve Reference
        if(index is KrashValueReference) index.toSimpleIndex(runtime)
        else index
    }.let {index ->

        // Resolve Index
        value.toSimple(runtime).let {value -> when(value) {

            // Array Element
            is KrashValueArray -> {

                // Integer Position
                if(index is KrashValueInteger) value.getElement(index.value)

                // Invalid Type
                else throw RuntimeException("Array indexes must be integers!")
                // NOTE: this is where custom exception handling should be added
            }

            // Map Key
            is KrashValueMap -> {

                // String Key
                if(index is KrashValueString) value.getData(index.value)

                // Invalid Type
                else throw RuntimeException("Map indexes must be strings!")
                // NOTE: this is where custom exception handling should be added
            }

            // String Character
            is KrashValueString -> {

                // Integer Position
                if(index is KrashValueInteger) value.getChar(index.value)

                // Invalid Type
                else throw RuntimeException("Character indexes must be integers!")
                // NOTE: come back to this; use custom exceptions later
            }

            // Invalid Type
            else -> throw RuntimeException("Cannot access index $index of this value!")
            // NOTE: come back to this; use custom exceptions later
        }}
    }

    override fun toString() = "$value[$index]"

}

interface KrashValueIndexPos {

    fun toSimpleIndex(runtime: KrashRuntime): KrashValueIndexPos {
        return this
    }

}

class KrashValueInteger(val value: Int): KrashValueSimple(),
    KrashValueIndexPos {

    override fun toString() = value.toString()

}

class KrashValueMap(val valueList: List<KrashValueMapPair>): KrashValueSimple() {

    // Create Data
    private val data = HashMap<String, KrashValue>().apply {
        valueList.forEach {
            put(it.key, it.value)
        }
    }

    init {

        // Append Members
        /*memberPut("contains") {runtime: KrashRuntime, argumentList: List<KrashValue> ->
            KrashValueBoolean(data.containsKey(argumentList[0].toSimple(runtime)))
        }*/
        // NOTE: need to get the above working
        memberPut("keys", KrashValueArray(data.map {
            KrashValueString(it.key)
        }))
    }

    fun getData() = data

    fun getData(key: String): KrashValue {

        // Return Element
        return if(data.containsKey(key)) data[key]!!

        // Invalid Index
        else throw RuntimeException("Invalid key '$key' for map!")
    }

    override fun toSimple(runtime: KrashRuntime) =
        KrashValueMap(data.map {
            KrashValueMapPair(it.key, it.value.toSimple(runtime))
        })

    override fun toString() = valueList.joinToString(", ", "{", "}") {
        it.toString()
    }

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

    fun memberGet(key: String): KrashValue = memberList[key] ?: throw RuntimeException("No member '$key' exists on value!")

    fun memberPut(key: String, value: KrashValue) {
        memberList[key] = value
    }

    fun memberPut(key: String, value: (runtime: KrashRuntime, argumentList: List<KrashValue>) -> KrashValue) {
        memberList[key] = KrashValueCallable(value)
    }

    override fun resolve(runtime: KrashRuntime) = this

}

class KrashValueString(val value: String): KrashValueSimple(hashMapOf(
    Pair("size", KrashValueInteger(value.length)),
    Pair("toList",
        KrashValueCallable { runtime: KrashRuntime, argumentList: List<KrashValue> ->
            KrashValueArray(ArrayList<KrashValueString>().apply {
                var pos = 0
                while (pos < value.length) {
                    add(KrashValueString(value.substring(pos, pos + 1)))
                    pos++
                }
            })
        })
)), KrashValueIndexPos {

    fun getChar(pos: Int) = KrashValueString(pos.let {

        // Negative Position
        if (it < 0) value.length + it

        // Positive Position
        else it
    }.let { pos ->

        // Invalid Position
        if (pos >= value.length || pos < 0) throw RuntimeException("Character index $pos out of bounds for string length ${value.length}!")
        // NOTE: come back to this; use custom exceptions later

        // Fetch Character
        value.substring(pos, pos + 1)
        // NOTE: need to consider complex positions like [1, 3] (char 1 to 3)
    })

    override fun toString() = value

}

class KrashValueReference(val value: String, val byRef: Boolean): KrashValue, KrashValueIndexPos {

    override fun resolve(runtime: KrashRuntime): KrashValue {

        // Native Method
        if(KrashMethod.nativeContains(value)) return KrashMethod.nativeGet(value)

        // Native Object
        if(KrashValueClass.nativeContains(value)) return KrashValueClass.nativeGet(value)

        // Custom Reference
        return runtime.heapGet(value)
    }

    override fun toSimpleIndex(runtime: KrashRuntime) = this.toSimple(runtime).let {

        // Invalid Type
        if(it !is KrashValueIndexPos) throw RuntimeException("Value is not a valid index!")
        // NOTE: come back to this; use custom exceptions later

        // Return Index
        it as KrashValueIndexPos
    }

    override fun toString() = value

}