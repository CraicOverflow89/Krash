package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.KrashValue
import craicoverflow89.krash.components.KrashValueArray
import craicoverflow89.krash.components.KrashValueBoolean
import craicoverflow89.krash.components.KrashValueCallable
import craicoverflow89.krash.components.KrashValueInteger
import craicoverflow89.krash.components.KrashValueMap
import craicoverflow89.krash.components.KrashValueMapPair
import craicoverflow89.krash.components.KrashValueNull
import craicoverflow89.krash.components.KrashValueString

abstract class KrashExpressionLiteral: KrashExpression()

class KrashExpressionLiteralArray(private val value: List<KrashExpression>): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueArray(value.map {
        it.toValue(runtime)
    })

}

class KrashExpressionLiteralBoolean(private val value: Boolean): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueBoolean(value)

}

class KrashExpressionLiteralCallable(private val expressionList: List<KrashExpression>): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime): KrashValueCallable {

        // Create Heap
        //

        // NOTE: need to validate expressionList and iterate contents

        // Create Callable
        return KrashValueCallable { runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // TEMP
            KrashValueNull()
        }
    }

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

    fun toValue(runtime: KrashRuntime) = KrashValueMapPair(key, value.toValue(runtime))

}

class KrashExpressionLiteralNull(): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueNull()

}

class KrashExpressionLiteralString(private val value: String): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueString(value)

}