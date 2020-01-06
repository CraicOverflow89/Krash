package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.objects.KrashValueInteger

abstract class KrashExpressionOperator: KrashExpression()

class KrashExpressionOperatorAddition(private val first: Int, private val second: Int): KrashExpressionOperator() {

    override fun toValue(runtime: KrashRuntime) =
        KrashValueInteger(first + second)

}

class KrashExpressionOperatorDivision(private val first: Int, private val second: Int): KrashExpressionOperator() {

    override fun toValue(runtime: KrashRuntime) =
        KrashValueInteger(first / second)
    // NOTE: should probably add custom validation to prevent division by zero issues

}

class KrashExpressionOperatorMultiplication(private val first: Int, private val second: Int): KrashExpressionOperator() {

    override fun toValue(runtime: KrashRuntime) =
        KrashValueInteger(first * second)

}

class KrashExpressionOperatorSubtraction(private val first: Int, private val second: Int): KrashExpressionOperator() {

    override fun toValue(runtime: KrashRuntime) =
        KrashValueInteger(first - second)

}