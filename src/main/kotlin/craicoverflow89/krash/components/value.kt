package craicoverflow89.krash.components

import java.io.File

interface KrashValue {

    fun resolve(runtime: KrashRuntime): KrashValue

    fun toSimple(runtime: KrashRuntime): KrashValueSimple {
        var result: KrashValue = resolve(runtime)
        while(result !is KrashValueSimple) result = result.toSimple(runtime)
        return result
    }

}

interface KrashValueSimple: KrashValue {

    override fun resolve(runtime: KrashRuntime) = this

}

class KrashValueArray(val valueList: List<KrashValue>): KrashValueSimple {

    fun getElement(pos: Int): KrashValue {

        // Return Element
        return if(pos < valueList.size) valueList[pos]

        // Invalid Index
        else throw RuntimeException("Element index $pos out of bounds for array length ${valueList.size}!")
    }

    override fun toSimple(runtime: KrashRuntime) = KrashValueArray(valueList.map {
        it.toSimple(runtime)
    })

    override fun toString() = valueList.joinToString(", ", "[", "]") {
        it.toString()
    }

}

class KrashValueBoolean(val value: Boolean): KrashValueSimple {

    override fun toString() = if(value) "true" else "false"

}

open class KrashValueCallable(private val logic: (runtime: KrashRuntime, argumentList: List<KrashValue>) -> KrashValue): KrashValueSimple {

    fun invoke(runtime: KrashRuntime, argumentList: List<KrashValue>) = logic(runtime, argumentList)

    override fun toString() = "<callable>"

}

class KrashValueFile(private val path: String): KrashValueSimple {

    fun toFile() = File(path)

    override fun toString() = path

}

class KrashValueIndex(val value: KrashValue, val index: KrashValueIndexPos): KrashValue {

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

    fun withValue(value: KrashValue) = KrashValueIndex(value, index)

}

interface KrashValueIndexPos {

    fun toSimpleIndex(runtime: KrashRuntime): KrashValueIndexPos {
        return this
    }

}

class KrashValueInteger(val value: Int): KrashValueSimple, KrashValueIndexPos {

    override fun toString() = value.toString()

}

class KrashValueInvoke(private val value: KrashValue, private val argumentList: List<KrashValue>): KrashValue {

    override fun resolve(runtime: KrashRuntime): KrashValue = value.toSimple(runtime).let {

        // NOTE: above just checks if ref maps to something in the heap
        //       but it might be best to first check a different map, that contains BIFs

        // Invoke Callable
        if(it is KrashValueCallable) it.invoke(runtime, argumentList)

        // Invalid Type
        else throw RuntimeException("Could not invoke this non-callable value!")
        // NOTE: come back to this; use custom exceptions later
    }

    override fun toString() = "<invoke>"

    fun withValue(value: KrashValue) = KrashValueInvoke(value, argumentList)

}

class KrashValueMap(val valueList: List<KrashValueMapPair>): KrashValueSimple {

    // Create Data
    private val data = HashMap<String, KrashValue>().apply {
        valueList.forEach {
            put(it.key, it.value)
        }
    }

    fun getData() = data

    fun getData(key: String): KrashValue {

        // Return Element
        return if(data.containsKey(key)) data[key]!!

        // Invalid Index
        else throw RuntimeException("Invalid key '$key' for map!")
    }

    override fun toSimple(runtime: KrashRuntime) = KrashValueMap(data.map {
        KrashValueMapPair(it.key, it.value.toSimple(runtime))
    })

    override fun toString() = valueList.joinToString(", ", "{", "}") {
        it.toString()
    }

}

class KrashValueMapPair(val key: String, val value: KrashValue) {

    override fun toString() = "$key: $value"

}

class KrashValueMember(val value: KrashValue, val member: KrashValue): KrashValue {

    // NOTE: member should be limited to valid ref-like string of chars
    //       it makes no sense to have things like "string"."literal"
    //       however it works best in the parser as it is (so check types here)

    override fun resolve(runtime: KrashRuntime): KrashValue {

        // NOTE: need to lookup members (properties, methods, globals) for this value
        //       properties could be things like length (int) for string
        //       members could be things like join (string) for array
        //       globals could be things like type (?) that exist on supertype of value
        // NOTE: should have a new interface like KrashValueMemberType
        //       but it's just ref, indexes and invocations really

        // Resolution Logic
        fun resolve(member: String): KrashValue {

            // TEMP
            return value.toSimple(runtime).let {
                // NOTE: should be able to call getMember on any KrashValueSimple object
                when(it) {
                    is KrashValueString -> {
                        when(member) {
                            "size" -> return KrashValueInteger(it.value.length)
                            "toList" -> return KrashValueCallable(fun(_: KrashRuntime, _: List<KrashValue>): KrashValue {
                                return KrashValueArray(it.value.let{
                                    ArrayList<KrashValueString>().apply {
                                        var pos = 0
                                        while(pos < it.length) {
                                            add(KrashValueString(it.substring(pos, pos + 1)))
                                            pos ++
                                        }
                                    }
                                })
                            })

                            // TEMP
                            else -> KrashValueNull()
                        }
                    }
                    else -> KrashValueNull()
                }
            }
        }

        // Resolve Member
        return when(member) {
            is KrashValueIndex -> member.withValue(resolve("member.value"))
            is KrashValueInvoke -> member.withValue(resolve("member.value"))
            is KrashValueReference -> resolve(member.ref.value)
            else -> throw RuntimeException("Invalid member!")
        }
    }

}

class KrashValueNull: KrashValueSimple {

    override fun toString() = "null"

}

class KrashValueString(val value: String): KrashValueSimple, KrashValueIndexPos {

    fun getChar(pos: Int) = KrashValueString(pos.let {

        // Negative Position
        if(it < 0) value.length + it

        // Positive Position
        else it
    }.let{pos ->

        // Invalid Position
        if(pos >= value.length || pos < 0) throw RuntimeException("Character index $pos out of bounds for string length ${value.length}!")
        // NOTE: come back to this; use custom exceptions later

        // Fetch Character
        value.substring(pos, pos + 1)
        // NOTE: need to consider complex positions like [1, 3] (char 1 to 3)
    })

    override fun toString() = value

}

class KrashValueReference(val ref: KrashReference, val byRef: Boolean): KrashValue, KrashValueIndexPos {

    override fun resolve(runtime: KrashRuntime): KrashValue {

        // Native Method
        if(KrashMethod.nativeContains(ref.value)) return KrashMethod.nativeGet(ref.value)

        // Custom Reference
        return runtime.heapGet(ref)
    }

    override fun toSimpleIndex(runtime: KrashRuntime) = this.toSimple(runtime).let {

        // Invalid Type
        if(it !is KrashValueIndexPos) throw RuntimeException("Value is not a valid index!")
        // NOTE: come back to this; use custom exceptions later

        // Return Index
        it as KrashValueIndexPos
    }

    override fun toString() = ref.value

}