package craicoverflow89.krash.components

interface KrashMethod {

    fun invoke(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue

}

class KrashMethodNative(private val type: KrashMethodNativeType): KrashMethod {

    override fun invoke(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue {

        // TEMP IMPLEMENTATION
        when(type) {

            // echo(Any*) prints one or more values
            KrashMethodNativeType.ECHO -> {

                // Resolution Logic
                fun resolve(value: KrashValue): String = when(value) {

                    // Resolve Array
                    is KrashValueArray -> value.valueList.joinToString(", ", "[", "]") {
                        resolve(it)
                    }

                    // Resolve Map
                    is KrashValueMap -> value.valueList.joinToString(", ", "[", "]") {
                        "${it.key}: ${resolve(it.value)}"
                    }

                    // Resolve Reference
                    is KrashValueReference -> {
                        //if(!runtime.heapContains(it.ref))
                        // NOTE: come back to validation
                        resolve(runtime.heapGet(value.ref))
                    }


                    // Resolve Primitive
                    else -> value.toString()
                }

                // Print Arguments
                argumentList.forEach {
                    println(resolve(it))
                }
            }
        }

        // TEMP
        return KrashValueNull()
    }

}

enum class KrashMethodNativeType {
    ECHO
}

class KrashMethodReference(private val ref: KrashReference): KrashMethod {

    override fun invoke(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue {

        // TEMP
        return KrashValueNull()
    }

}