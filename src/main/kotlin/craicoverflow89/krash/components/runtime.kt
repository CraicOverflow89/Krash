package craicoverflow89.krash.components

import craicoverflow89.krash.components.objects.KrashValue
import craicoverflow89.krash.components.objects.KrashValueClass
import craicoverflow89.krash.components.objects.KrashValueNull
import craicoverflow89.krash.components.objects.KrashValueReference
import kotlin.system.exitProcess

class KrashHeap(private val runtime: KrashRuntime, private val parent: KrashHeap?) {

    // Define Heap
    private val heap = HashMap<String, KrashValue>()

    fun contains(ref: String): Boolean {

        // Key Exists
        if(heap.containsKey(ref)) return true

        // Parent Heap
        return parent?.contains(ref) ?: false
    }

    fun get(ref: String): KrashValue {

        // Fetch Value
        return heap[ref] ?: parent?.get(ref) ?: throw RuntimeException("Reference '$ref' does not exist!")
    }

    fun put(ref: String, value: KrashValue) {

        // Persist Reference
        if(
            // Standard Reference
            (value is KrashValueReference && value.byRef)
        /*||
            // Indexed Reference
            (value is KrashValueIndex && value.value is KrashValueReference && value.value.byRef)
            // NOTE: need to sort this with expressions
        */
        ) {
            heap[ref] = value
            // NOTE: might want to prevent circular references from being created with &ref
        }

        // Resolve Values
        else heap[ref] = value.toSimple(runtime)
    }

}

class KrashReference(val value: String)

class KrashReserved {

    companion object {

        private val reservedTerms = ArrayList<String>().apply {

            // Structural Keyword
            addAll(listOf("fun", "return"))

            // Literal Keywords
            addAll(listOf("false", "null", "true"))

            // Native Methods
            addAll(KrashMethod.nativeReserved())

            // Native Classes
            addAll(KrashValueClass.nativeReserved())
        }

        fun contains(value: String) = reservedTerms.contains(value.toLowerCase())

    }

}

class KrashRuntime(cwd: String? = null, parentHeap: KrashHeap? = null) {

    companion object {

        // Define Path
        private var cwdPath = ""

        fun cwd() = cwdPath

        fun cwdJoin(value: String) = "$cwdPath/$value"

        fun cwdSet(path: String) {
            cwdPath = path.replace("\\", "/")
        }

    }

    init {

        // Update CWD
        if(cwd != null) cwdSet(cwd)
    }

    // Define Heap
    private val heap = KrashHeap(this, parentHeap)

    fun child() = KrashRuntime(null, heap)

    fun exit(code: Int) {
        exitProcess(code)
    }

    fun heapContains(ref: KrashReference) = heap.contains(ref.value)
    // NOTE: replace with String when KrashReference is removed altogether

    fun heapGet(ref: String) = heap.get(ref)

    fun heapPut(ref: String, value: KrashValue) = heap.put(ref, value)

}

class KrashScript(private val commandList: List<KrashCommand>) {

    fun invoke(cwd: String) {

        // Create Runtime
        val runtime = KrashRuntime(cwd)

        // Invoke Commands
        commandList.forEach {
            it.invoke(runtime)
        }

    }

}