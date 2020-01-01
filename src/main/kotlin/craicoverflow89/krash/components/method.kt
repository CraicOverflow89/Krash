package craicoverflow89.krash.components

interface KrashMethod {

    fun invoke(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue

}

class KrashMethodNative(private val type: KrashMethodNativeType): KrashMethod {

    override fun invoke(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue {

        // TEMP IMPLEMENTATION
        when(type) {
            KrashMethodNativeType.ECHO -> {
                argumentList.forEach {
                    println(if(it is KrashValueReference) {
                        //if(!runtime.heapContains(it.ref))
                        // NOTE: come back to validation
                        runtime.heapGet(it.ref, true).toString()
                        // NOTE: consider references to other references (should this even exist?)
                    } else it.toString())
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