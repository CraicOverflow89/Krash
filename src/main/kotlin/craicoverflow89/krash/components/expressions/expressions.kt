package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.KrashValue
import craicoverflow89.krash.components.KrashValueReference

abstract class KrashExpression {

    abstract fun toValue(runtime: KrashRuntime): KrashValue

}

class KrashExpressionReference(private val value: String, private val byRef: Boolean): KrashExpression() {

    override fun toValue(runtime: KrashRuntime) = KrashValueReference(value, byRef)

}