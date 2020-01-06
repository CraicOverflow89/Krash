package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.objects.*

abstract class KrashExpressionLiteral: KrashExpression()

class KrashExpressionLiteralArray(private val value: List<KrashExpression>): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueArray(ArrayList<KrashValue>().apply {
        value.forEach {
            add(it.toValue(runtime))
        }
    })

}

class KrashExpressionLiteralBoolean(private val value: Boolean): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueBoolean(value)

}

class KrashExpressionLiteralCallable(private val argumentList: List<KrashExpressionLiteralCallableArgument>, private val expressionList: List<KrashExpressionLiteralCallableExpression>): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime): KrashValueCallable {

        // Create Heap
        val callableRuntime = runtime.child()
        val callableArgs = argumentList

        // Create Callable
        return KrashValueCallable {_: KrashRuntime, argumentList: List<KrashValue> ->

            // Inject Arguments
            callableArgs.forEachIndexed {pos, arg ->
                callableRuntime.heapPut(arg.name, if(pos < argumentList.size) argumentList[pos] else arg.defaultValue(runtime))
            }

            // Implicit It
            if(argumentList.size == 1) callableRuntime.heapPut("it", argumentList[0])

            // Default Result
            var returnValue: KrashValue = KrashValueNull()

            // Iterate Expressions
            var result: KrashValue
            var pos = 0
            while(pos < expressionList.size) {

                // Invoke Expression
                result = expressionList[pos].toValue(callableRuntime)

                // Return Result
                if(expressionList[pos].isReturn) {
                    returnValue = result
                    break
                }

                // Next Expression
                pos ++
            }

            // Return Result
            returnValue
        }
    }

}

class KrashExpressionLiteralCallableArgument(val name: String, private val defaultValue: KrashExpression? = null) {

    fun defaultValue(runtime: KrashRuntime) = defaultValue?.toValue(runtime) ?: KrashValueNull()

}

class KrashExpressionLiteralCallableExpression(private val expression: KrashExpression, val isReturn: Boolean): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = expression.toValue(runtime)

}

class KrashExpressionLiteralDouble(private val value: Double): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueDouble(value)

}

class KrashExpressionLiteralInteger(private val value: Int): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueInteger(value)

}

class KrashExpressionLiteralMap(private val value: List<KrashExpressionLiteralMapPair>): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueMap(value.map {
        it.toValue(runtime)
    })

}

class KrashExpressionLiteralMapPair(private val key: String, private val value: KrashExpression) {

    fun toValue(runtime: KrashRuntime) =
        KrashValueMapPair(key, value.toValue(runtime))

}

class KrashExpressionLiteralNull: KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueNull()

}

class KrashExpressionLiteralString(private val value: String): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueString(value)

}