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

                    // Resolve Index
                    is KrashValueIndex -> resolve(value.resolve(runtime))

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
                    //println(resolve(it))
                    println(it.toSimple(runtime))
                }
            }

            // file(String?) creates a file object (defaults to cwd)
            KrashMethodNativeType.FILE -> {

                // Current Directory
                return KrashValueFile(if(argumentList.isEmpty()) runtime.cwd()

                // Path Supplied
                else argumentList[0].toSimple(runtime).toString().let {

                    // Relative Path
                    if(it.startsWith(".")) runtime.cwdJoin(it)

                    // Absolute Path
                    else it
                })
            }
        }

        // TEMP
        return KrashValueNull()
    }

}

enum class KrashMethodNativeType {
    ECHO, FILE
}

class KrashMethodValue(private val value: KrashValue): KrashMethod {

    override fun invoke(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue {

        // Resolution Logic
        fun resolve(value: KrashValue): KrashValueCallable {

            // Resolve Index
            if(value is KrashValueIndex) return resolve(value.resolve(runtime))

            // Resolve Reference
            if(value is KrashValueReference) return resolve(runtime.heapGet(value.ref))

            // Return Callable
            if(value is KrashValueCallable) return value

            // Invalid Type
            throw RuntimeException("Encountered an exception when invoking a value!")
            // NOTE: come back to this; use custom exceptions later
        }

        // Invoke Callable
        //resolve(value).invoke(runtime, argumentList)
        // NOTE: invoke the logic, wherever that is actually performed

        // TEMP
        return KrashValueNull()
    }

}