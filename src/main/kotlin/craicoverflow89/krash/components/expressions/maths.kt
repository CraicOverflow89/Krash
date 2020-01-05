package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.KrashValueInteger

abstract class KrashExpressionMaths: KrashExpression()

class KrashExpressionMathsAddition(private val first: Int, private val second: Int): KrashExpressionMaths() {

    override fun toValue(runtime: KrashRuntime) = KrashValueInteger(first + second)

}

class KrashExpressionMathsDivision(private val first: Int, private val second: Int): KrashExpressionMaths() {

    override fun toValue(runtime: KrashRuntime) = KrashValueInteger(first / second)
    // NOTE: should probably add custom validation to prevent division by zero issues

}

class KrashExpressionMathsMultiplication(private val first: Int, private val second: Int): KrashExpressionMaths() {

    override fun toValue(runtime: KrashRuntime) = KrashValueInteger(first * second)

}

class KrashExpressionMathsSubtraction(private val first: Int, private val second: Int): KrashExpressionMaths() {

    override fun toValue(runtime: KrashRuntime) = KrashValueInteger(first - second)

}