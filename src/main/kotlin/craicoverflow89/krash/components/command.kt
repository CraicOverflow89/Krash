package craicoverflow89.krash.components

interface KrashCommand {

    fun invoke(runtime: KrashRuntime): KrashValue

}

class KrashCommandDeclare(private val ref: KrashReference, private val value: KrashValue): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue {

        // Reserved Term
        if(KrashReserved.contains(ref.value)) {
            throw RuntimeException("Could not create a reference with the reserved term ''")
            // NOTE: come back to this; use custom exceptions later
        }

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

class KrashCommandValue(private val value: KrashValue): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue {
        return value.resolve(runtime)
    }

}