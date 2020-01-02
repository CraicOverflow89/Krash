package craicoverflow89.krash.components

class KrashReference(val value: String) {

    // fun lookup() ??

}

class KrashRuntime {

    // Define Heap
    val heap = HashMap<String, KrashValue>()

    fun heapContains(ref: KrashReference) = heap.containsKey(ref.value)

    fun heapGet(ref: KrashReference): KrashValue {
        fun resolve(ref: KrashReference): KrashValue {
            var result = heap[ref.value] ?: KrashValueNull()
            if(result is KrashValueReference) result = resolve(result.ref)
            return result
        }
        return resolve(ref)
    }

    fun heapPut(ref: KrashReference, value: KrashValue) {

        // Resolve References
        if(value is KrashValueReference && !value.byRef) {
            heap[ref.value] = heapGet(value.ref)
            // NOTE: since removing recurse flag, need to look at new references to arrays with nested byRef and byValue
        }
        // NOTE: might want to prevent circular references from being created with &ref

        // Persist References
        else heap[ref.value] = value
    }

}

class KrashScript(private val commandList: List<KrashCommand>) {

    fun invoke() {

        // Create Runtime
        val runtime = KrashRuntime()

        // Invoke Commands
        commandList.forEach {
            it.invoke(runtime)
        }

    }

}