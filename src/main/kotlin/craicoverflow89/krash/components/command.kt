package craicoverflow89.krash.components

interface KrashCommand {

    fun invoke(runtime: KrashRuntime): KrashValue

}

class KrashCommandDeclare(private val ref: KrashReference, private val value: KrashValue): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue {

        // Reserved Term
        if(KrashReserved.contains(ref.value)) {
            throw RuntimeException("Could not create a reference with the reserved term '${ref.value}'")
            // NOTE: come back to this; use custom exceptions later
        }

        // Update Heap
        runtime.heapPut(ref, value)

        // Return Null
        return KrashValueNull()
    }

}

class KrashCommandValue(private val value: KrashValue): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue {
        return value.resolve(runtime)
    }

}