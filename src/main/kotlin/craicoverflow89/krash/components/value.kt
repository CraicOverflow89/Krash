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

class KrashValueIndex(val ref: KrashValueReference, val indexList: List<String>): KrashValue {

    override fun resolve(runtime: KrashRuntime): KrashValue {

        // Resolution Logic
        fun resolve(value: KrashValue, index: String): KrashValue = when(value) {

            // Array Position
            is KrashValueArray -> value.valueList[Integer.parseInt(index)]
            // NOTE: maybe KrashValueArray should have a method for getElement() (returns KrashValueNull if none found?)
            // NOTE: this is where custom exception handling should be added
            //       java.lang.NumberFormatException is being thrown here

            // Map Key
            is KrashValueMap -> value.getData(index)

            // Invalid Type
            else -> throw RuntimeException("Cannot access index $index of this value!")
            // NOTE: come back to this; use custom exceptions later
        }

        // Resolve Indexes
        var result: KrashValue = runtime.heapGet(ref.ref)
        var indexPos = 0
        while(indexPos < indexList.size) {
            result = resolve(result, indexList[indexPos])
            indexPos ++
        }
        return result
    }

    override fun toString() = "$ref${indexList.joinToString("") {
        "[$it]"
    }}"

}

class KrashValueInteger(val value: Int): KrashValueSimple {

    override fun toString() = value.toString()

}

class KrashValueInvoke(private val ref: KrashValueReference, private val argumentList: List<KrashValue>): KrashValue {

    override fun resolve(runtime: KrashRuntime): KrashValue = ref.resolve(runtime).let {

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

class KrashValueString(val value: String): KrashValueSimple {

    override fun toString() = value

}

class KrashValueReference(val ref: KrashReference, val byRef: Boolean): KrashValue {

    override fun resolve(runtime: KrashRuntime): KrashValue {

        // Native Method
        if(KrashMethod.nativeContains(ref.value)) return KrashMethod.nativeGet(ref.value)

        // Custom Reference
        return runtime.heapGet(ref)
    }

    override fun toString() = ref.value

}