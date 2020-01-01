package craicoverflow89.krash.components

interface KrashCommand {

    fun invoke(runtime: KrashRuntime): KrashValue

}

class KrashCommandDeclare(private val ref: KrashReference, private val value: KrashValue): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue {

        // Update Heap
        runtime.heapPut(ref, value)

        // Return Null
        return KrashValueNull()
    }

}

class KrashCommandInvoke(private val method: KrashMethod, private val argumentList: List<KrashValue>): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue {

        // NOTE: currently passing all arguments by value
        return method.invoke(runtime, argumentList)
    }

}