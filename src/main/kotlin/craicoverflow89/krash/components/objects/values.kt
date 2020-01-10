package craicoverflow89.krash.components.objects

import craicoverflow89.krash.components.KrashRuntimeException
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

    fun toStringType() = KrashValueString(this.toString())

}

class KrashValueArray(private val valueList: ArrayList<KrashValue> = arrayListOf()): KrashValueSimple(hashMapOf(
    Pair("add", KrashValueCallable {_: KrashRuntime, argumentList: List<KrashValue> ->
        valueList.add(argumentList[0])
        KrashValueNull()
    }),
    Pair("join", KrashValueCallable {runtime: KrashRuntime, argumentList: List<KrashValue> ->

        // Parse Arguments
        val separator = if(argumentList.isNotEmpty()) argumentList[0].toSimple(runtime).let {
            if(it !is KrashValueString) throw KrashRuntimeException("Invalid type for separator!")
            it.getValue()
        } else ""
        val prefix: String = if(argumentList.size > 1) argumentList[1].toSimple(runtime).let {
            if(it !is KrashValueString) throw KrashRuntimeException("Invalid type for prefix!")
            it.getValue()
        } else ""
        val postfix = if(argumentList.size > 2) argumentList[2].toSimple(runtime).let {
            if(it !is KrashValueString) throw KrashRuntimeException("Invalid type for postfix!")
            it.getValue()
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
            if(argumentList.isEmpty()) throw KrashRuntimeException("No value provided for logic!")

            // Validate Logic
            val logic: KrashValueCallable = argumentList[0].toSimple(runtime).let {
                if(it !is KrashValueCallable) throw KrashRuntimeException("Logic must be callable!")
                it
            }

            // Invoke Logic
            valueList.forEach {
                logic.invoke(runtime, listOf(it))
            }

            // Return Array
            this
        }
        memberPut("eachIndexed") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Validate Arguments
            if(argumentList.isEmpty()) throw KrashRuntimeException("No value provided for logic!")

            // Validate Logic
            val logic: KrashValueCallable = argumentList[0].toSimple(runtime).let {
                if(it !is KrashValueCallable) throw KrashRuntimeException("Logic must be callable!")
                it
            }

            // Invoke Logic
            valueList.forEachIndexed {index, value ->
                logic.invoke(runtime, listOf(KrashValueInteger(index), value))
            }

            // Return Array
            this
        }
        memberPut("filter") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Validate Arguments
            if(argumentList.isEmpty()) throw KrashRuntimeException("No value provided for logic!")

            // Validate Logic
            val logic: KrashValueCallable = argumentList[0].toSimple(runtime).let {
                if(it !is KrashValueCallable) throw KrashRuntimeException("Logic must be callable!")
                it
            }

            // Create Result
            val result = ArrayList<KrashValue>()

            // Invoke Logic
            valueList.forEach {value ->
                logic.invoke(runtime, listOf(value)).let {

                    // Validate Return
                    if(it !is KrashValueBoolean) throw KrashRuntimeException("Logic must return boolean!")

                    // Include Element
                    if(it.isTrue()) result.add(value)
                }
            }

            // Return Result
            KrashValueArray(result)
        }
        memberPut("map") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Validate Arguments
            if(argumentList.isEmpty()) throw KrashRuntimeException("No value provided for logic!")

            // Validate Logic
            val logic: KrashValueCallable = argumentList[0].toSimple(runtime).let {
                if(it !is KrashValueCallable) throw KrashRuntimeException("Logic must be callable!")
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
            if(argumentList.isEmpty()) throw KrashRuntimeException("No value provided for logic!")

            // Validate Logic
            val logic: KrashValueCallable = argumentList[0].toSimple(runtime).let {
                if(it !is KrashValueCallable) throw KrashRuntimeException("Logic must be callable!")
                it
            }

            // Create Result
            val result = ArrayList<KrashValue>()

            // Invoke Logic
            valueList.forEach {value ->
                logic.invoke(runtime, listOf(value)).let {

                    // Validate Return
                    if(it !is KrashValueBoolean) throw KrashRuntimeException("Logic must return boolean!")

                    // Include Element
                    if(!it.isTrue()) result.add(value)
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
        else throw KrashRuntimeException("Element index $pos out of bounds for array length ${valueList.size}!")
    }

    fun getSize() = valueList.size

    fun getValue() = valueList

    fun setElement(pos: Int, value: KrashValue) {

        // New Element
        if(pos == valueList.size) valueList.add(value)

        // Update Element
        else if(pos >= 0 && pos < valueList.size) valueList[pos] = value

        // Invalid Index
        else throw KrashRuntimeException("Element index $pos out of bounds for array length ${valueList.size}!")
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
            if(KrashReserved.contains(it.key)) throw KrashRuntimeException("Cannot use reserved term '${it.key}' for map key!")

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
                if(it !is KrashValueString) throw KrashRuntimeException("Invalid type for map key!")

                // Assign Value
                data[it.getValue()] = argumentList[1]
            }

            // Done
            KrashValueNull()
        }
        memberPut("contains") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Resolve Key
            argumentList[0].toSimple(runtime).let {

                // Invalid Type
                if(it !is KrashValueString) throw KrashRuntimeException("Invalid type for map key!")

                // Return Result
                KrashValueBoolean(data.containsKey(it.getValue()))
            }
        }
        memberPut("each") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Validate Arguments
            if(argumentList.isEmpty()) throw KrashRuntimeException("No value provided for logic!")

            // Validate Logic
            val logic: KrashValueCallable = argumentList[0].toSimple(runtime).let {
                if(it !is KrashValueCallable) throw KrashRuntimeException("Logic must be callable!")
                it
            }

            // Invoke Logic
            data.forEach {
                logic.invoke(runtime, listOf(KrashValueString(it.key), it.value))
            }

            // Return Map
            this
        }
        memberPut("filter") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Validate Arguments
            if(argumentList.isEmpty()) throw KrashRuntimeException("No value provided for logic!")

            // Validate Logic
            val logic: KrashValueCallable = argumentList[0].toSimple(runtime).let {
                if(it !is KrashValueCallable) throw KrashRuntimeException("Logic must be callable!")
                it
            }

            // Create Result
            val result = ArrayList<KrashValueMapPair>()

            // Invoke Logic
            data.forEach {k, v ->
                logic.invoke(runtime, listOf(KrashValueString(k), v)).let {

                    // Validate Return
                    if(it !is KrashValueBoolean) throw KrashRuntimeException("Logic must return boolean!")

                    // Include Element
                    if(it.isTrue()) result.add(KrashValueMapPair(k, v))
                }
            }

            // Return Result
            KrashValueMap(result)
        }
        memberPut("keys") {_: KrashRuntime, _: List<KrashValue> ->
            KrashValueArray(ArrayList<KrashValue>().apply {
                data.forEach {
                    add(KrashValueString(it.key))
                }
            })
        }
        memberPut("map") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Validate Arguments
            if(argumentList.isEmpty()) throw KrashRuntimeException("No value provided for logic!")

            // Validate Logic
            val logic: KrashValueCallable = argumentList[0].toSimple(runtime).let {
                if(it !is KrashValueCallable) throw KrashRuntimeException("Logic must be callable!")
                it
            }

            // Create Result
            val result = ArrayList<KrashValueMapPair>()

            // Invoke Logic
            data.forEach {k, v ->
                result.add(KrashValueMapPair(k, logic.invoke(runtime, listOf(KrashValueString(k), v))))
            }

            // Return Result
            KrashValueMap(result)
        }
        memberPut("reject") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // Validate Arguments
            if(argumentList.isEmpty()) throw KrashRuntimeException("No value provided for logic!")

            // Validate Logic
            val logic: KrashValueCallable = argumentList[0].toSimple(runtime).let {
                if(it !is KrashValueCallable) throw KrashRuntimeException("Logic must be callable!")
                it
            }

            // Create Result
            val result = ArrayList<KrashValueMapPair>()

            // Invoke Logic
            data.forEach {k, v ->
                logic.invoke(runtime, listOf(KrashValueString(k), v)).let {

                    // Validate Return
                    if(it !is KrashValueBoolean) throw KrashRuntimeException("Logic must return boolean!")

                    // Include Element
                    if(!it.isTrue()) result.add(KrashValueMapPair(k, v))
                }
            }

            // Return Result
            KrashValueMap(result)
        }
    }

    fun getData() = data

    fun getData(key: String): KrashValue {

        // Return Element
        return if(data.containsKey(key)) data[key]!!

        // Invalid Index
        else throw KrashRuntimeException("Invalid key '$key' for map!")
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

    fun isEqual(value: KrashValueObject): Boolean {

        // Compare Class
        if(obj.name != value.obj.name) return false

        // Compare Content
        return serialiseExists() && serialiseData() == value.serialiseData()
    }

    private fun serialiseData() = memberGet("serialise").let {

        // Custom String
        if(it is KrashValueString) it.getValue()

        // Cast String
        else it.toString()
    }

    private fun serialiseExists() = memberContains("serialise")

    override fun toString(): String {

        // Invoke Member
        if(memberContains("toString")) memberGet("toString").let {

            // Custom String
            if(it is KrashValueString) it.getValue()

            // Cast String
            else it.toString()
        }

        // Default Value
        return "<object ${obj.name}>"
    }

}

abstract class KrashValueSimple(private val memberList: HashMap<String, KrashValue> = hashMapOf()): KrashValue {

    fun memberContains(key: String) = memberList.containsKey(key)

    fun memberGet(key: String): KrashValue = when {

        // Get Member
        memberList.containsKey(key) -> memberList[key]!!

        // Default Method
        key == "toString" -> KrashValueCallable { _: KrashRuntime, _: List<KrashValue> ->
            KrashValueString("this.toString()")
        }

        // Invalid Key
        else -> throw KrashRuntimeException("No member '$key' exists on value!")
    }

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

class KrashValueString(private val value: String): KrashValueSimple() {

    init {
        // NOTE: convert "$ref" pieces if valueInitial contains '$'

        // Append Members
        memberPut("endsWith", KrashValueCallable {_: KrashRuntime, argumentList: List<KrashValue> ->

            // Validate Characters
            if(argumentList.isEmpty()) throw KrashRuntimeException("No value provided for characters!")

            // Parse Characters
            KrashValueBoolean(argumentList[0].let {

                // Invalid Type
                if(it !is KrashValueString) throw KrashRuntimeException("Invalid type for characters!")

                // Return Result
                value.endsWith(it.value)
            })
        })
        memberPut("toList", KrashValueCallable {_: KrashRuntime, argumentList: List<KrashValue> ->
            KrashValueArray(ArrayList<KrashValue>().apply {

                // Use Delimiter
                if(argumentList.isNotEmpty()) argumentList[0].let {

                    // Invalid Type
                    if(it !is KrashValueString) throw KrashRuntimeException("Invalid type for delimiter!")

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
        })
        memberPut("size", KrashValueInteger(value.length))
        memberPut("startsWith", KrashValueCallable {_: KrashRuntime, argumentList: List<KrashValue> ->

            // Validate Characters
            if(argumentList.isEmpty()) throw KrashRuntimeException("No value provided for characters!")

            // Parse Characters
            KrashValueBoolean(argumentList[0].let {

                // Invalid Type
                if(it !is KrashValueString) throw KrashRuntimeException("Invalid type for characters!")

                // Return Result
                value.startsWith(it.value)
            })
        })
    }

    fun getChar(pos: Int) = KrashValueString(pos.let {

        // Negative Position
        if (it < 0) value.length + it

        // Positive Position
        else it
    }.let {pos ->

        // Invalid Position
        if (pos >= value.length || pos < 0) throw KrashRuntimeException("Character index $pos out of bounds for string length ${value.length}!")

        // Fetch Character
        value.substring(pos, pos + 1)
        // NOTE: need to consider complex positions like [1, 3] (char 1 to 3)
    })

    fun getValue() = value

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