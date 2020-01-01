package craicoverflow89.krash.components

class KrashReference(val value: String) {

    // fun lookup() ??

}

class KrashRuntime {

    // Define Heap
    val heap = HashMap<String, KrashValue>()

    fun heapContains(ref: KrashReference) = heap.containsKey(ref.value)

    fun heapGet(ref: KrashReference, recurse : Boolean = false): KrashValue {
        fun resolve(ref: KrashReference): KrashValue {
            var result = heap[ref.value] ?: KrashValueNull()
            if(result is KrashValueReference && recurse) result = resolve(result.ref)
            return result
        }
        return resolve(ref)
    }

    fun heapPut(ref: KrashReference, value: KrashValue) {
        // NOTE: might allow for KrashValueReference to have a byRef / byValue flag
        //       which will determine if value becomes reference or takes value of that reference
        heap[ref.value] = value
    }

}

class KrashScript(private val commandList: List<KrashCommand>) {

    fun invoke() {
        //
    }

}