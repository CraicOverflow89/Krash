package craicoverflow89.krash.components

import craicoverflow89.krash.KrashException
import craicoverflow89.krash.components.objects.*
import kotlin.system.exitProcess

abstract class KrashChannel {

    abstract fun err(text: String)

    open fun read(): String {
        throw KrashChannelException("Input is not supported by this channel!")
    }

    abstract fun out(text: String)

}

class KrashChannelException(message: String): KrashException(message)

class KrashChannelShell: KrashChannel() {

    override fun err(text: String) {
        System.err.println(text)
    }

    override fun read(): String {
        return readLine() ?: ""
    }

    override fun out(text: String) {
        println(text)
    }

}

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
        return heap[ref] ?: parent?.get(ref) ?: throw KrashRuntimeException("Reference '$ref' does not exist!")
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

class KrashReserved {

    companion object {

        private val reservedTerms = ArrayList<String>().apply {

            // Structural Keyword
            addAll(listOf("else", "fun", "if", "return", "while"))

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
        private var channel: KrashChannel = KrashChannelShell()
        private var userDirectoryPath = System.getProperty("user.home").replace("\\", "/")
        private var scriptPath: KrashValueString? = null
        private var scriptArgs: KrashValueArray = KrashValueArray()

        fun channelSet(value: KrashChannel) {
            channel = value
        }

        fun createScript(cwd: String, file: KrashValueString, args: List<KrashValueString>): KrashRuntime {

            // Set Values
            scriptPath = file
            scriptArgs = KrashValueArray(ArrayList<KrashValue>().apply {
                args.forEach {
                    add(it)
                }
            })

            // Return Runtime
            return KrashRuntime(cwd)
        }

        fun cwd() = cwdPath

        fun cwdJoin(value: String) = "$cwdPath/$value"

        fun cwdSet(path: String) {
            cwdPath = path.replace("\\", "/")
        }

        fun cwdString() = KrashValueString(cwdPath)

        fun error(value: Any) = channel.err(value.toString())

        fun global(value: String): KrashValueSimple = when(value) {

            // Script Arguments
            "ARGS" -> scriptArgs

            // Working Directory
            "CWD" -> cwdString()

            // User Directory
            "HOME" -> userDirectoryString()

            // Invalid Value
            else -> throw KrashRuntimeException("Constant '$value' does not exist!")
        }

        fun globalContains(value: String) = listOf("ARGS", "CWD", "HOME").contains(value)
        // NOTE: this needs to be merged with the above (single map of data)

        fun println(value: Any) = channel.out(value.toString())

        fun read() = channel.read()

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

class KrashRuntimeException(message: String): KrashException(message)

class KrashScript(private val commandList: List<KrashCommand>) {

    fun invoke(cwd: String, file: String, args: List<String>) {

        // Create Runtime
        val runtime = KrashRuntime.createScript(cwd, KrashValueString(file), args.map {
            KrashValueString(it)
        })

        // Iterate Commands
        commandList.forEach {

            // Invoke Command
            try {it.invoke(runtime)}

            // Error Handling
            catch(ex: KrashRuntimeException) {
                KrashRuntime.error(ex.message())
            }
        }

    }

}