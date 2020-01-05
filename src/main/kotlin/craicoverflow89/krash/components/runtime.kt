package craicoverflow89.krash.components

import craicoverflow89.krash.components.objects.KrashValue
import craicoverflow89.krash.components.objects.KrashValueClass
import craicoverflow89.krash.components.objects.KrashValueIndex
import craicoverflow89.krash.components.objects.KrashValueNull
import craicoverflow89.krash.components.objects.KrashValueReference
import kotlin.system.exitProcess

class KrashReference(val value: String)

class KrashReserved {

    companion object {

        private val reservedTerms = ArrayList<String>().apply {

            // Structural Keyword
            addAll(listOf("fun"))
            // NOTE: this is where if/else/while could be added

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

class KrashRuntime(cwd: String) {

    // Define Path
    private val cwdPath = cwd.replace("\\", "/")

    // Define Heap
    val heap = HashMap<String, KrashValue>()

    fun cwd() = cwdPath

    fun cwdJoin(value: String) = "$cwdPath/$value"

    fun exit(code: Int) {
        exitProcess(code)
    }

    fun heapContains(ref: KrashReference) = heap.containsKey(ref.value)

    fun heapGet(ref: String) = heap[ref] ?: KrashValueNull()

    fun heapPut(ref: String, value: KrashValue) {

        // Persist Reference
        if(
            // Standard Reference
            (value is KrashValueReference && value.byRef)
        ||
            // Indexed Reference
            (value is KrashValueIndex && value.value is KrashValueReference && value.value.byRef)
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