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
    Pair("join", KrashValueCallable {runtime: KrashRuntime, argumentList: List<KrashValue> ->

        // Parse Arguments
        val separator = if(argumentList.isNotEmpty()) argumentList[0].toSimple(runtime).let {
            if(it !is KrashValueString) throw KrashException("Invalid type for separator!")
            it.value
        } else ""
        val prefix: String = if(argumentList.size > 1) argumentList[1].toSimple(runtime).let {
            if(it !is KrashValueString) throw KrashException("Invalid type for prefix!")
            it.value
        } else ""
        val postfix = if(argumentList.size > 2) argumentList[2].toSimple(runtime).let {
            if(it !is KrashValueString) throw KrashException("Invalid type for postfix!")
            it.value
        } else ""

        // Return Result
        KrashValueString(valueList.joinToString(separator, prefix, postfix))
    }),
    Pair("size", KrashValueCallable {_: KrashRuntime, _: List<KrashValue> ->
        KrashValueInteger(valueList.size)
    })
)) {

    init {

        // Append Members
        memberPut("each") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Validate Arguments
            if(argumentList.isEmpty()) throw KrashException("No value provided for logic!")

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
        memberPut("filter") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Validate Arguments
            if(argumentList.isEmpty()) throw KrashException("No value provided for logic!")

            // Validate Logic
            val logic: KrashValueCallable = argumentList[0].toSimple(runtime).let {
                if(it !is KrashValueCallable) throw KrashException("Logic must be callable!")
                it
            }

            // Create Result
            val result = ArrayList<KrashValue>()

            // Invoke Logic
            valueList.forEach {
                logic.invoke(runtime, listOf(it)).let {

                    // Validate Return
                    if(it !is KrashValueBoolean) throw KrashException("Logic must return boolean!")

                    // Include Element
                    if(it.isTrue()) result.add(it)
                }
            }

            // Return Result
            KrashValueArray(result)
        }
        memberPut("map") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Validate Arguments
            if(argumentList.isEmpty()) throw KrashException("No value provided for logic!")

            // Validate Logic
            val logic: KrashValueCallable = argumentList[0].toSimple(runtime).let {
                if(it !is KrashValueCallable) throw KrashException("Logic must be callable!")
                it
            }

            // Create Result
            val result = ArrayList<KrashValue>()

            // Invoke Logic
            valueList.forEach {
                result.add(logic.invoke(runtime, listOf(it)))
            }

            // Return Result
            KrashValueArray(result)
        }
        memberPut("reject") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Validate Arguments
            if(argumentList.isEmpty()) throw KrashException("No value provided for logic!")

            // Validate Logic
            val logic: KrashValueCallable = argumentList[0].toSimple(runtime).let {
                if(it !is KrashValueCallable) throw KrashException("Logic must be callable!")
                it
            }

            // Create Result
            val result = ArrayList<KrashValue>()

            // Invoke Logic
            valueList.forEach {
                logic.invoke(runtime, listOf(it)).let {

                    // Validate Return
                    if(it !is KrashValueBoolean) throw KrashException("Logic must return boolean!")

                    // Include Element
                    if(!it.isTrue()) result.add(it)
                }
            }

            // Return Result
            KrashValueArray(result)
        }
    }

    fun getElement(pos: Int): KrashValue {

        // Return Element
        return if(pos >= 0 && pos < valueList.size) valueList[pos]

        // Invalid Index
        else throw KrashException("Element index $pos out of bounds for array length ${valueList.size}!")
    }

    fun setElement(pos: Int, value: KrashValue) {

        // New Element
        if(pos == valueList.size) valueList.add(value)

        // Update Element
        else if(pos >= 0 && pos < valueList.size) valueList[pos] = value

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

    fun isTrue() = value

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
            argumentList[0].toSimple(runtime).let {

                // Invalid Type
                if(it !is KrashValueString) throw KrashException("Invalid type for map key!")

                // Assign Value
                data[it.value] = argumentList[1]
            }

            // Done
            KrashValueNull()
        }
        memberPut("contains") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Resolve Key
            argumentList[0].toSimple(runtime).let {

                // Invalid Type
                if(it !is KrashValueString) throw KrashException("Invalid type for map key!")

                // Return Result
                KrashValueBoolean(data.containsKey(it.value))
            }
        }
        memberPut("keys") {_: KrashRuntime, _: List<KrashValue> ->
            KrashValueArray(ArrayList<KrashValue>().apply {
                data.forEach {
                    add(KrashValueString(it.key))
                }
            })
        }
    }

    fun getData() = data

    fun getData(key: String): KrashValue {

        // Return Element
        return if(data.containsKey(key)) data[key]!!

        // Invalid Index
        else throw KrashException("Invalid key '$key' for map!")
    }

    fun setData(key: String, value: KrashValue) {
        data[key] = value
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
    Pair("endsWith", KrashValueCallable {_: KrashRuntime, argumentList: List<KrashValue> ->

        // Validate Characters
        if(argumentList.isEmpty()) throw KrashException("No value provided for characters!")

        // Parse Characters
        KrashValueBoolean(argumentList[0].let {

            // Invalid Type
            if(it !is KrashValueString) throw KrashException("Invalid type for characters!")

            // Return Result
            value.endsWith(it.value)
        })
    }),
    Pair("toList", KrashValueCallable {_: KrashRuntime, argumentList: List<KrashValue> ->
        KrashValueArray(ArrayList<KrashValue>().apply {

            // Use Delimiter
            if(argumentList.isNotEmpty()) argumentList[0].let {

                // Invalid Type
                if(it !is KrashValueString) throw KrashException("Invalid type for delimiter!")

                // Split String
                value.split(it.value).forEach {
                    add(KrashValueString(it))
                }
            }

            // Char List
            else {
                var pos = 0
                while(pos < value.length) {
                    add(KrashValueString(value.substring(pos, pos + 1)))
                    pos ++
                }
            }
        })
    }),
    Pair("size", KrashValueInteger(value.length)),
    Pair("startsWith", KrashValueCallable {_: KrashRuntime, argumentList: List<KrashValue> ->

        // Validate Characters
        if(argumentList.isEmpty()) throw KrashException("No value provided for characters!")

        // Parse Characters
        KrashValueBoolean(argumentList[0].let {

            // Invalid Type
            if(it !is KrashValueString) throw KrashException("Invalid type for characters!")

            // Return Result
            value.startsWith(it.value)
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