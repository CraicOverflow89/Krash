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
            throw KrashException("Could not create a reference with the reserved term '${ref}'")
        }

        // Update Heap
        runtime.heapPut(ref, value.toValue(runtime))

        // Return Null
        return KrashValueNull()
    }

}

class KrashCommandExpression(private val value: KrashExpression): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue = value.toValue(runtime)

}