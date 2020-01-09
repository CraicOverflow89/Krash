package craicoverflow89.krash.components

import craicoverflow89.krash.components.objects.*
import kotlin.system.exitProcess

class KrashException(message: String): RuntimeException(message)

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
        return heap[ref] ?: parent?.get(ref) ?: throw KrashException("Reference '$ref' does not exist!")
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

abstract class KrashOutput {

    abstract fun err(text: String)

    abstract fun out(text: String)

}

class KrashOutputShell: KrashOutput() {

    override fun err(text: String) {
        System.err.println(text)
    }

    override fun out(text: String) {
        println(text)
    }

}

class KrashReserved {

    companion object {

        private val reservedTerms = ArrayList<String>().apply {

            // Structural Keyword
            addAll(listOf("fun", "if", "return"))

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

        // Define Properties
        private var cwdPath = ""
        private var output: KrashOutput = KrashOutputShell()
        private var userDirectoryPath = System.getProperty("user.home").replace("\\", "/")

        fun cwd() = cwdPath

        fun cwdJoin(value: String) = "$cwdPath/$value"

        fun cwdSet(path: String) {
            cwdPath = path.replace("\\", "/")
        }

        fun cwdString() = KrashValueString(cwdPath)

        fun error(value: Any) = output.err(value.toString())

        fun global(value: String): KrashValueSimple = when(value) {

            // Working Directory
            "CWD" -> cwdString()

            // User Directory
            "HOME" -> userDirectoryString()

            // Invalid Value
            else -> throw KrashException("Constant '$value' does not exist!")
        }

        fun println(value: Any) = output.out(value.toString())

        fun outputSet(value: KrashOutput) {
            output = value
        }

        fun userDirectoryString() = KrashValueString(userDirectoryPath)

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

    fun heapContains(ref: String) = heap.contains(ref)

    fun heapGet(ref: String) = heap.get(ref)

    fun heapPut(ref: String, value: KrashValue) = heap.put(ref, value)

}

class KrashScript(private val commandList: List<KrashCommand>) {

    fun invoke(cwd: String) {

        // Create Runtime
        val runtime = KrashRuntime(cwd)

        // Iterate Commands
        commandList.forEach {

            // Invoke Command
            try {it.invoke(runtime)}

            // Error Handling
            catch(ex: RuntimeException) {
                KrashRuntime.println("ERROR: ${ex.message}")
            }
        }

    }

}