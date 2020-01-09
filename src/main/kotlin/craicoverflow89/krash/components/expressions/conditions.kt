package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashRuntimeException
import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.objects.KrashValueBoolean
import craicoverflow89.krash.components.objects.KrashValueNull
import craicoverflow89.krash.components.objects.KrashValueSimple
import craicoverflow89.krash.components.objects.KrashValueSimpleNumeric
import craicoverflow89.krash.components.objects.KrashValueString

abstract class KrashExpressionCondition(private val first: KrashExpression, private val second: KrashExpression): KrashExpression() {

    abstract fun invoke(first: KrashValueSimple, second: KrashValueSimple): Boolean

    override fun toValue(runtime: KrashRuntime) = KrashValueBoolean(invoke(first.toValue(runtime), second.toValue(runtime)))

}

class KrashExpressionConditionEquality(first: KrashExpression, second: KrashExpression): KrashExpressionCondition(first, second) {

    override fun invoke(first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Compare Boolean
        is KrashValueBoolean -> when(second) {
            is KrashValueBoolean -> first.isTrue() == second.isTrue()
            else -> throw KrashRuntimeException("Invalid type for equality comparison with boolean!")
        }

        // Compare Null
        is KrashValueNull -> when(second) {
            is KrashValueNull -> true
            else -> throw KrashRuntimeException("Invalid type for equality comparison with null!")
        }

        // Compare Numbers
        is KrashValueSimpleNumeric -> when(second) {
            is KrashValueSimpleNumeric -> first.toDouble() == second.toDouble()
            else -> throw KrashRuntimeException("Invalid type for equality comparison with number!")
        }

        // Compare Strings
        is KrashValueString -> when(second) {
            is KrashValueString -> first.getValue() == second.getValue()
            else -> throw KrashRuntimeException("Invalid type for equality comparison with string!")
        }

        // Invalid Type
        else -> throw KrashRuntimeException("Invalid type for equality comparison!")
    }

}

class KrashExpressionConditionGreater(first: KrashExpression, second: KrashExpression): KrashExpressionCondition(first, second) {

    override fun invoke(first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Compare Numbers
        is KrashValueSimpleNumeric -> when(second) {
            is KrashValueSimpleNumeric -> first.toDouble() > second.toDouble()
            else -> throw KrashRuntimeException("Invalid type for greater than comparison with number!")
        }

        // Invalid Type
        else -> throw KrashRuntimeException("Invalid type for greater than comparison!")
    }

}

class KrashExpressionConditionInequality(first: KrashExpression, second: KrashExpression): KrashExpressionCondition(first, second) {

    override fun invoke(first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Compare Boolean
        is KrashValueBoolean -> when(second) {
            is KrashValueBoolean -> first.isTrue() != second.isTrue()
            else -> throw KrashRuntimeException("Invalid type for inequality comparison with boolean!")
        }

        // Compare Null
        is KrashValueNull -> when(second) {
            is KrashValueNull -> false
            else -> throw KrashRuntimeException("Invalid type for inequality comparison with null!")
        }

        // Compare Numbers
        is KrashValueSimpleNumeric -> when(second) {
            is KrashValueSimpleNumeric -> first.toDouble() != second.toDouble()
            else -> throw KrashRuntimeException("Invalid type for inequality comparison with number!")
        }

        // Compare Strings
        is KrashValueString -> when(second) {
            is KrashValueString -> first.getValue() != second.getValue()
            else -> throw KrashRuntimeException("Invalid type for inequality comparison with string!")
        }

        // Invalid Type
        else -> throw KrashRuntimeException("Invalid type for inequality comparison!")
    }

}

class KrashExpressionConditionLesser(first: KrashExpression, second: KrashExpression): KrashExpressionCondition(first, second) {

    override fun invoke(first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Compare Numbers
        is KrashValueSimpleNumeric -> when(second) {
            is KrashValueSimpleNumeric -> first.toDouble() < second.toDouble()
            else -> throw KrashRuntimeException("Invalid type for lesser than comparison with number!")
        }

        // Invalid Type
        else -> throw KrashRuntimeException("Invalid type for lesser than comparison!")
    }

}