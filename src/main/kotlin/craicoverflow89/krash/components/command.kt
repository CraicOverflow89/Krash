package craicoverflow89.krash.components

import craicoverflow89.krash.components.expressions.KrashExpression
import craicoverflow89.krash.components.objects.KrashValue
import craicoverflow89.krash.components.objects.KrashValueNull

interface KrashCommand {

    fun invoke(runtime: KrashRuntime): KrashValue

}

class KrashCommandComment(private val value: String): KrashCommand {

    override fun invoke(runtime: KrashRuntime) = KrashValueNull()

}

class KrashCommandDeclare(private val ref: String, private val value: KrashExpression): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue {

        // Reserved Term
        if(KrashReserved.contains(ref)) {
            throw RuntimeException("Could not create a reference with the reserved term '${ref}'")
            // NOTE: come back to this; use custom exceptions later
        }
        // NOTE: maybe there should be a KrashReference.isValid / validate method instead of writing logic here

        // Update Heap
        runtime.heapPut(ref, value.toValue(runtime))

        // Return Null
        return KrashValueNull()
    }

}

class KrashCommandExpression(private val value: KrashExpression): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue = value.toValue(runtime)

}