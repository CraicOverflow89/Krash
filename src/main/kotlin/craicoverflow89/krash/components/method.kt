package craicoverflow89.krash.components

interface KrashMethod {

    fun invoke(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue

}

class KrashMethodNative(private val type: KrashMethodNativeType): KrashMethod {

    override fun invoke(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue {

        // TEMP IMPLEMENTATION
        when(type) {
            KrashMethodNativeType.ECHO -> {

                // Resolution Logic
                fun resolve(value: KrashValue): String = when(value) {

                    // Resolve Array
                    is KrashValueArray -> value.valueList.map {
                        resolve(it)
                    }.joinToString(", ", "[", "]")


                    // Resolve Reference
                    is KrashValueReference -> {
                        //if(!runtime.heapContains(it.ref))
                        // NOTE: come back to validation
                        runtime.heapGet(value.ref, true).toString()
                    }


                    // Resolve String
                    else -> value.toString()
                }

                // Print Result
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