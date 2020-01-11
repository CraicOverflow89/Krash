package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.*
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

class KrashExpressionStructureWhile(private val condition: KrashExpression, private val commandList: ArrayList<KrashCommand>): KrashExpressionStructure() {

    override fun toValue(runtime: KrashRuntime): KrashValueSimple {

        // Define Position
        var pos = 0

        // Keyword Listeners
        var isBreak = false
        var isContinue = false
        runtime.apply {
            keywordListenerAdd(KrashCommandKeywordType.BREAK) {
                isBreak = true
            }
            keywordListenerAdd(KrashCommandKeywordType.CONTINUE) {
                isContinue = true
            }
        }

        // Check Condition
        while(condition.toValue(runtime).let {

            // Return Result
            if(it is KrashValueBoolean) it.isTrue()

            // Invalid Type
            else throw KrashRuntimeException("Condition must be boolean!")
        }) {

            // Get Command
            val command = commandList[pos]

            // Invoke Command
            command.invoke(runtime)

            // Terminate Loop
            if(isBreak) break

            // Reset Position
            if(isContinue) {
                isContinue = false
                pos = 0
                continue
            }

            // Next Command
            pos ++

            // Restart Loop
            if(pos == commandList.size) pos = 0
        }

        // Clear Listeners
        runtime.keywordListenerClear()

        // Done
        return KrashValueNull()
    }

}