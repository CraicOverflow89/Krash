package craicoverflow89.krash.components

interface KrashCommand {

    fun invoke(runtime: KrashRuntime): KrashValue

}

class KrashCommandDeclare(private val ref: String, private val value: KrashValue): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue {

        // Reserved Term
        if(KrashReserved.contains(ref)) {
            throw RuntimeException("Could not create a reference with the reserved term '${ref}'")
            // NOTE: come back to this; use custom exceptions later
        }
        // NOTE: maybe there should be a KrashReference.isValid / validate method instead of writing logic here

        // Update Heap
        runtime.heapPut(ref, value)

        // Return Null
        return KrashValueNull()
    }

}

class KrashCommandValue(private val value: KrashValue): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue {
        return value.toSimple(runtime)
    }

}