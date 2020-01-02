package craicoverflow89.krash.components

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

    override fun toString() = valueList.joinToString(", ", "[", "]") {
        it.toString()
    }

}

class KrashValueBoolean(val value: Boolean): KrashValueSimple {

    override fun toString() = if(value) "true" else "false"

}

class KrashValueCallable: KrashValue {

    override fun resolve(runtime: KrashRuntime): KrashValue {

        // TEMP
        return KrashValueNull()
    }

    override fun toString() = "<callable>"

}

class KrashValueIndex(val ref: KrashValueReference, val pos: Int): KrashValue {

    override fun resolve(runtime: KrashRuntime): KrashValue = runtime.heapGet(ref.ref).let {
        when(it) {

            // Resolve Array
            is KrashValueArray -> it.valueList[pos]

            // Resolve Map
            //is KrashValueMap -> value.getData()[pos]
            // NOTE: maybe use a different class for keys instead (not indexes) ??

            // Invalid Type
            else -> throw RuntimeException("Cannot access index $pos of this value!")
            // NOTE: come back to this; use custom exceptions later
        }
    }

    override fun toString() = "$ref[$pos]"

}

class KrashValueInteger(val value: Int): KrashValueSimple {

    override fun toString() = value.toString()

}

class KrashValueMap(val valueList: List<KrashValueMapPair>): KrashValueSimple {

    // Create Data
    private val data = HashMap<String, KrashValue>().apply {
        valueList.forEach {
            put(it.key, it.value)
        }
    }

    fun getData() = data

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

    override fun resolve(runtime: KrashRuntime) = runtime.heapGet(ref)

    override fun toString() = ref.value

}