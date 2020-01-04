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

    fun getElement(pos: Int) = valueList[pos]
    // NOTE: java.lang.NumberFormatException is being thrown here

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

}

class KrashValueMap(val valueList: List<KrashValueMapPair>): KrashValueSimple {

    // Create Data
    private val data = HashMap<String, KrashValue>().apply {
        valueList.forEach {
            put(it.key, it.value)
        }
    }

    fun getData() = data

    fun getData(key: String) = data[key] ?: KrashValueNull()
    // NOTE: null safety is great but there should be a custom runtime exception being thrown here

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