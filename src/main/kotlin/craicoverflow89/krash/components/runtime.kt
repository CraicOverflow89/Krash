package craicoverflow89.krash.components

import craicoverflow89.krash.components.objects.KrashValue
import craicoverflow89.krash.components.objects.KrashValueClass
import craicoverflow89.krash.components.objects.KrashValueNull
import craicoverflow89.krash.components.objects.KrashValueReference
import kotlin.system.exitProcess

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

class KrashRuntime(cwd: String, private val parent: KrashRuntime? = null) {

    // Define Path
    private val cwdPath = cwd.replace("\\", "/")

    // Define Heap
    val heap = HashMap<String, KrashValue>()

    fun child() = KrashRuntime(cwdPath, this)

    fun cwd() = cwdPath

    fun cwdJoin(value: String) = "$cwdPath/$value"

    fun exit(code: Int) {
        exitProcess(code)
    }

    fun heapContains(ref: KrashReference): Boolean {

        // Key Exists
        if(heap.containsKey(ref.value)) return true

        // Parent Heap
        return parent?.heapContains(ref) ?: false
    }

    fun heapGet(ref: String): KrashValue {

        // Fetch Value
        return heap[ref] ?: parent?.heapGet(ref) ?: throw RuntimeException("Reference '$ref' does not exist!")
    }

    fun heapPut(ref: String, value: KrashValue) {

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
        else heap[ref] = value.toSimple(this)
    }

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