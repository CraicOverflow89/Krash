package craicoverflow89.krash.components

import craicoverflow89.krash.KrashException
import craicoverflow89.krash.components.expressions.KrashExpressionLiteralCallableArgument
import craicoverflow89.krash.components.expressions.KrashExpressionLiteralCallableArgumentModifier
import craicoverflow89.krash.components.objects.*
import craicoverflow89.krash.system.KrashFileSystem
import java.io.File
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

    fun clear() {
        heap.clear()
    }

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

    fun keys() = heap.keys

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
            addAll(listOf("break", "class", "continue", "else", "fun", "if", "is", "return", "when", "while"))

            // Literal Keywords
            addAll(listOf("false", "null", "true"))

            // Default Methods
            addAll(listOf("apply", "let"))

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
        private val classData = HashMap<String, KrashValueClass>()
        private val enumData = HashMap<String, KrashValueEnum>()
        private val methodData = HashMap<String, KrashValueCallable>()

        fun channelSet(value: KrashChannel) {
            channel = value
        }

        fun classExists(name: String) = classData.contains(name)

        fun classGet(name: String): KrashValueClass {

            // Invalid Name
            if(!classExists(name)) throw KrashRuntimeException("Could not find '$name' class!")

            // Return Class
            return classData[name]!!
        }

        fun classRegister(name: String, value: KrashValueClass) {

            // Reserved Term
            if(KrashReserved.contains(name)) throw KrashRuntimeException("Cannot use reserved term '$name' for class!")

            // Existing Class
            if(classData.containsKey(name)) throw KrashRuntimeException("Cannot duplicate term '$name' for class!")

            // Register Class
            classData[name] = value
        }

        fun create(cwd: String, args: List<String>): KrashRuntime {

            // Inject Arguments
            scriptArgs = KrashValueArray(ArrayList<KrashValue>().apply {
                args.forEach {
                    add(KrashValueString(it))
                }
            })

            // Return Runtime
            return KrashRuntime(cwd)
        }

        fun cwd() = cwdPath

        fun cwdJoin(value: String) = listOf(cwdPath, value.replace("\\", "/").let {
            if(it.startsWith("/")) it.substring(1, it.length) else it
        }).joinToString("/")

        fun cwdSet(path: String) {
            cwdPath = path.replace("\\", "/").let {
                if(it.endsWith("/")) it.substring(0, it.length - 1) else it
            }
        }

        fun cwdString() = KrashValueString(cwdPath)

        fun enumExists(name: String) = enumData.contains(name)

        fun enumGet(name: String): KrashValueEnum {

            // Invalid Name
            if(!enumExists(name)) throw KrashRuntimeException("Could not find '$name' enum!")

            // Return Enum
            return enumData[name]!!
        }

        fun enumRegister(name: String, value: KrashValueEnum) {

            // Reserved Term
            if(KrashReserved.contains(name)) throw KrashRuntimeException("Cannot use reserved term '$name' for enum!")

            // Existing Enum
            if(enumData.containsKey(name)) throw KrashRuntimeException("Cannot duplicate term '$name' for enum!")

            // Register Class
            enumData[name] = value
        }

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

        fun methodData() = methodData

        fun methodExists(name: String) = methodData.contains(name)

        fun methodGet(name: String): KrashValueCallable {

            // Invalid Name
            if(!methodExists(name)) throw KrashRuntimeException("Could not find '$name' method!")

            // Return Method
            return methodData[name]!!
        }

        fun methodRegister(name: String, value: KrashValueCallable) {

            // Reserved Term
            if(KrashReserved.contains(name)) throw KrashRuntimeException("Cannot use reserved term '$name' for method!")

            // Existing Method
            if(methodData.containsKey(name)) throw KrashRuntimeException("Cannot duplicate term '$name' for method!")

            // Register Method
            methodData[name] = value
        }

        fun println(value: Any) = channel.out(value.toString())

        fun read() = channel.read()

        fun userDirectoryString() = KrashValueString(userDirectoryPath)

    }

    init {

        // Update CWD
        if(cwd != null) cwdSet(cwd)
    }

    // Define Properties
    private val heap = KrashHeap(this, parentHeap)
    private var keywordListenerData = HashMap<KrashCommandKeywordType, () -> Unit>()
    private var returnListenerData: ((KrashValueSimple) -> Unit)? = null

    fun child() = KrashRuntime(null, heap)

    fun empty() {

        // Empty Classes
        classData.clear()

        // Empty Enums
        enumData.clear()

        // Empty Methods
        methodData.clear()

        // Empty Heap
        heap.clear()

        // Empty Listeners
        keywordListenerClear()
        returnListenerClear()
    }

    fun exit(code: Int) {
        exitProcess(code)
    }

    fun heapContains(ref: String) = heap.contains(ref)

    fun heapData() = heap.keys()

    fun heapGet(ref: String) = heap.get(ref)

    fun heapInject(parentRuntime: KrashRuntime, namedArgs: List<KrashExpressionLiteralCallableArgument>, invokeArgs: List<KrashValue>) {
        namedArgs.forEachIndexed {pos, arg ->
            heapPut(arg.name, invokeArgs.let {

                // Argument Value
                if(pos < invokeArgs.size) invokeArgs[pos].let {

                    // Cast String
                    if(arg.modifier == KrashExpressionLiteralCallableArgumentModifier.STRING) it.toStringType()

                    // Keep Value
                    else it
                }

                // Default Value
                else arg.defaultValue(parentRuntime)
            })
        }
    }

    fun heapPut(ref: String, value: KrashValue) {
        heap.put(ref, value)
    }

    fun includeScript(path: String) {

        // Load File
        File(path.let {

            // Contains Extension
            if(it.endsWith(".krash")) it

            // Append Extension
            else "$it.krash"
        }.let {

            // Absolute Path
            if(KrashFileSystem.isAbsolutePath(it)) it

            // Relative Path
            else cwdJoin(it)
        }).let {

            // Missing File
            if(!it.exists()) throw KrashRuntimeException("Could not find script!")

            // Invalid File
            if(it.extension != "krash") throw KrashRuntimeException("Must be a krash script!")

            // Update Path
            scriptPath = KrashValueString(it.absolutePath)

            // Invoke Script
            KrashInterpreter.parseScript(it.readText()).invoke(this)
        }
    }

    fun keywordListenerAdd(type: KrashCommandKeywordType, logic: () -> Unit) {
        keywordListenerData[type] = logic
    }

    fun keywordListenerClear() {
        keywordListenerData.clear()
    }

    fun keywordListenerInvoke(type: KrashCommandKeywordType) = keywordListenerData[type]?.let {

        // Invoke Logic
        it.invoke()

        // Return True
        true
    } ?: false

    fun returnListenerAdd(logic: (KrashValueSimple) -> Unit) {
        returnListenerData = logic
    }

    fun returnListenerClear() {
        returnListenerData = null
    }

    fun returnListenerInvoke(value: KrashValueSimple) = returnListenerData?.let {

        // Invoke Logic
        it.invoke(value)

        // Return True
        true
    } ?: false

}

class KrashRuntimeException(message: String): KrashException(message)

class KrashScript(private val commandList: List<KrashCommand>) {

    fun invoke(runtime: KrashRuntime) {

        // Iterate Commands
        commandList.forEach {

            // Invoke Command
            try {it.invoke(runtime)}

            // Error Handling
            catch(ex: KrashException) {
                KrashRuntime.error(ex.message())
            }
        }

    }

}