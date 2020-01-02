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

class KrashMethodValue(private val value: KrashValue): KrashMethod {

    override fun invoke(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue {

        // TEMP DEBUG
        println("KrashMethodValue invoke")
        println("  value: $value")

        // Resolution Logic
        fun resolve(value: KrashValue): KrashValueCallable {

            // Resolve Reference
            if(value is KrashValueReference) return resolve(runtime.heapGet(value.ref))

            // Return Callable
            if(value is KrashValueCallable) return value

            // Invalid Type
            throw RuntimeException("Encountered an exception when invoking a value!")
            // NOTE: very temporary; use custom exceptions later
        }

        // TEMP DEBUG
        println("  type:  ${(resolve(value).javaClass.name)}")

        // Invoke Callable
        //resolve(value).invoke(runtime, argumentList)
        // NOTE: invoke the logic, wherever that is actually performed

        // TEMP
        return KrashValueNull()
    }

}