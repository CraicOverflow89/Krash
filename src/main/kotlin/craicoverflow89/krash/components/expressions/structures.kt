package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashException
import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.objects.KrashValueBoolean
import craicoverflow89.krash.components.objects.KrashValueNull
import craicoverflow89.krash.components.objects.KrashValueSimple

abstract class KrashExpressionStructure: KrashExpression()

class KrashExpressionStructureIf(private val condition: KrashExpression, private val expressionListTrue: List<KrashExpression>, private val expressionListElse: List<KrashExpression>): KrashExpressionStructure() {

    override fun toValue(runtime: KrashRuntime): KrashValueSimple {

        // Define Result
        var result: KrashValueSimple = KrashValueNull()

        // Resolve Condition
        if(condition.toValue(runtime).let {

            // Return Result
            if(it is KrashValueBoolean) it.isTrue()

            // Invalid Type
            else throw KrashException("Condition must be boolean!")
        }) {

            // Execute Body
            expressionListTrue.forEach {
                result = it.toValue(runtime)
            }
        }

        // Execute Else
        else expressionListElse.forEach {
            result = it.toValue(runtime)
        }

        // Return Result
        return result
    }

}