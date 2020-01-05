package craicoverflow89.krash.components

abstract class KrashExpression {

    abstract fun resolve(runtime: KrashRuntime): KrashValue

}

class KrashExpressionLiteralBoolean(private val value: Boolean): KrashExpression() {

    override fun resolve(runtime: KrashRuntime) = KrashValueBoolean(value)

}

class KrashExpressionLiteralCallable(private val expressionList: List<KrashExpression>): KrashExpression() {

    override fun resolve(runtime: KrashRuntime): KrashValueCallable {

        // Create Heap
        //

        // NOTE: need to validate expressionList and iterate contents

        // Create Callable
        return KrashValueCallable {runtime: KrashRuntime, argumentList: List<KrashValue> ->

            // TEMP
            KrashValueNull()
        }
    }

}

class KrashExpressionLiteralInteger(private val value: Int): KrashExpression() {

    override fun resolve(runtime: KrashRuntime) = KrashValueInteger(value)

}

class KrashExpressionLiteralNull(private val value: Int): KrashExpression() {

    override fun resolve(runtime: KrashRuntime) = KrashValueNull()

}

class KrashExpressionLiteralString(private val value: String): KrashExpression() {

    override fun resolve(runtime: KrashRuntime) = KrashValueString(value)

}

class KrashExpressionReference(private val value: String, private val byRef: Boolean): KrashExpression() {

    //override fun resolve(runtime: KrashRuntime) = KrashValueReference(value, byRef)
    // TEMP
    override fun resolve(runtime: KrashRuntime) = KrashValueNull()

}