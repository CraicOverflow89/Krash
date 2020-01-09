package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashCommand
import craicoverflow89.krash.components.KrashRuntimeException
import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.objects.KrashValue
import craicoverflow89.krash.components.objects.KrashValueBoolean
import craicoverflow89.krash.components.objects.KrashValueNull
import craicoverflow89.krash.components.objects.KrashValueSimple

abstract class KrashExpressionStructure: KrashExpression()

class KrashExpressionStructureIf(private val condition: KrashExpression, private val commandTrue: List<KrashCommand>, private val commandElse: List<KrashCommand>): KrashExpressionStructure() {

    override fun toValue(runtime: KrashRuntime): KrashValueSimple {

        // Define Result
        var result: KrashValue = KrashValueNull()

        // Resolve Condition
        if(condition.toValue(runtime).let {

            // Return Result
            if(it is KrashValueBoolean) it.isTrue()

            // Invalid Type
            else throw KrashRuntimeException("Condition must be boolean!")
        }) {

            // Execute Body
            commandTrue.forEach {
                result = it.invoke(runtime)
            }
        }

        // Execute Else
        else commandElse.forEach {
            result = it.invoke(runtime)
        }

        // Return Result
        return result.toSimple(runtime)
    }

}

class KrashExpressionStructureWhile(private val condition: KrashExpression, private val commandTrue: ArrayList<KrashCommand>): KrashExpressionStructure() {

    override fun toValue(runtime: KrashRuntime): KrashValueSimple {

        // Resolve Condition
        while(condition.toValue(runtime).let {

            // Return Result
            if(it is KrashValueBoolean) it.isTrue()

            // Invalid Type
            else throw KrashRuntimeException("Condition must be boolean!")
        }) {

            // Execute Body
            commandTrue.forEach {
                it.invoke(runtime)
            }
        }

        // Done
        return KrashValueNull()
    }

}