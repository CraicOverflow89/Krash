package craicoverflow89.krash.components

interface KrashCommand {

    fun invoke(runtime: KrashRuntime): KrashValue

}

class KrashCommandDeclare(private val ref: KrashReference, private val value: KrashValue): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue {

        // Update Heap
        runtime.heapPut(ref, value)

        // TEMP DEBUG
        println("")
        println("KrashCommandDeclare")
        println(" ref:      ${ref.value}")
        println(" contains: ${runtime.heapContains(ref)}")
        println(" get:      ${runtime.heapGet(ref)}")
        println("")

        // Return Null
        return KrashValueNull()
    }

}

class KrashCommandInvoke(private val method: KrashMethod, private val argumentList: List<KrashCommandInvokeArgument>): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue {

        // NOTE: need to execute method and return the result

        // TEMP
        return KrashValueNull()
    }

}

class KrashCommandInvokeArgument(private val value: KrashValue)