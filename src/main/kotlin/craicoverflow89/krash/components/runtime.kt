package craicoverflow89.krash.components

class KrashReference(val value: String)

class KrashReserved {

    companion object {

        private val reservedTerms = listOf("echo", "fun")

        fun contains(value: String) = reservedTerms.contains(value.toLowerCase())

    }

}

class KrashRuntime {

    // Define Heap
    val heap = HashMap<String, KrashValue>()

    fun heapContains(ref: KrashReference) = heap.containsKey(ref.value)

    fun heapGet(ref: KrashReference) = heap[ref.value] ?: KrashValueNull()

    fun heapPut(ref: KrashReference, value: KrashValue) {

        // Keep Reference
        if(value is KrashValueReference && value.byRef) {
            heap[ref.value] = value
            // NOTE: might want to prevent circular references from being created with &ref
        }

        // Indexed Reference
        else if(value is KrashValueIndex && value.ref.byRef) {
            heap[ref.value] = value
        }

        // Resolve Values
        else heap[ref.value] = value.toSimple(this)
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