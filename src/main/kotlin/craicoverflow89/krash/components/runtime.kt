package craicoverflow89.krash.components

class KrashReference(val value: String) {

    // fun lookup() ??

}

class KrashRuntime {

    // Define Heap
    val heap = HashMap<String, KrashValue>()

    fun heapContains(ref: KrashReference) = heap.containsKey(ref.value)

    fun heapGet(ref: KrashReference): KrashValue {
        return heap[ref.value] ?: KrashValueNull()
    }

    fun heapPut(ref: KrashReference, value: KrashValue) {
        heap[ref.value] = value
    }

}

class KrashScript(private val commandList: List<KrashCommand>) {

    fun invoke() {
        //
    }

}