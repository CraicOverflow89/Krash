package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashRuntimeException
import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.objects.KrashValueArray
import craicoverflow89.krash.components.objects.KrashValueBoolean
import craicoverflow89.krash.components.objects.KrashValueCallable
import craicoverflow89.krash.components.objects.KrashValueClass
import craicoverflow89.krash.components.objects.KrashValueDouble
import craicoverflow89.krash.components.objects.KrashValueEnum
import craicoverflow89.krash.components.objects.KrashValueInteger
import craicoverflow89.krash.components.objects.KrashValueMap
import craicoverflow89.krash.components.objects.KrashValueNull
import craicoverflow89.krash.components.objects.KrashValueObject
import craicoverflow89.krash.components.objects.KrashValueSimple
import craicoverflow89.krash.components.objects.KrashValueSimpleNumeric
import craicoverflow89.krash.components.objects.KrashValueString

abstract class KrashExpressionCondition(private val first: KrashExpression, private val second: KrashExpression): KrashExpression() {

    abstract fun invoke(runtime: KrashRuntime, first: KrashValueSimple, second: KrashValueSimple): Boolean

    override fun toValue(runtime: KrashRuntime) = KrashValueBoolean(invoke(runtime, first.toValue(runtime), second.toValue(runtime)))

}

class KrashExpressionConditionEquality(first: KrashExpression, second: KrashExpression): KrashExpressionCondition(first, second) {

    override fun invoke(runtime: KrashRuntime, first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // NOTE: all KrashValueSimple classes will have an isEqual method to invoke here

        // Compare Array
        /*is KrashValueArray -> when(second) {
            is KrashValueArray -> first.getSize() == second.getSize() && first.getValue().let {firstValue ->
                val secondValue = second.getValue()
                var pos = 0
                while(pos < firstValue.size) {
                    if(firstValue[pos] != secondValue[pos]) return false
                    pos ++
                }
                true
            }
            else -> throw KrashRuntimeException("Invalid type for inequality comparison with array!")
        }*/
        // NOTE: need to runtime equality check for these values

        // Compare Boolean
        is KrashValueBoolean -> when(second) {
            is KrashValueBoolean -> first.isTrue() == second.isTrue()
            else -> throw KrashRuntimeException("Invalid type for equality comparison with boolean!")
        }

        // Compare Class
        is KrashValueClass -> when(second) {
            is KrashValueClass -> first.getName() == second.getName()
            else -> throw KrashRuntimeException("Invalid type for equality comparison with class!")
        }

        // Compare Null
        is KrashValueNull -> when(second) {
            is KrashValueNull -> true
            else -> throw KrashRuntimeException("Invalid type for equality comparison with null!")
        }

        // Compare Object
        is KrashValueObject -> when(second) {
            is KrashValueObject -> first.isEqual(second)
            else -> throw KrashRuntimeException("Invalid type for equality comparison with object!")
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

class KrashExpressionConditionGreaterEqual(first: KrashExpression, second: KrashExpression): KrashExpressionCondition(first, second) {

    override fun invoke(runtime: KrashRuntime, first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Compare Numbers
        is KrashValueSimpleNumeric -> when(second) {
            is KrashValueSimpleNumeric -> first.toDouble() >= second.toDouble()
            else -> throw KrashRuntimeException("Invalid type for greater than or equal to comparison with number!")
        }

        // Invalid Type
        else -> throw KrashRuntimeException("Invalid type for greater than comparison!")
    }

}

class KrashExpressionConditionGreaterThan(first: KrashExpression, second: KrashExpression): KrashExpressionCondition(first, second) {

    override fun invoke(runtime: KrashRuntime, first: KrashValueSimple, second: KrashValueSimple) = when(first) {

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

    // NOTE: this should probably not be a separate class - need a boolean argument for equal / not equal check
    //       and just flip the result to determine outcome of the condition
    //       exception message will also be partially determined by equal / not equal value

    override fun invoke(runtime: KrashRuntime, first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Compare Boolean
        is KrashValueBoolean -> when(second) {
            is KrashValueBoolean -> first.isTrue() != second.isTrue()
            else -> throw KrashRuntimeException("Invalid type for inequality comparison with boolean!")
        }

        // Compare Class
        is KrashValueClass -> when(second) {
            is KrashValueClass -> first.getName() != second.getName()
            else -> throw KrashRuntimeException("Invalid type for equality comparison with class!")
        }

        // Compare Null
        is KrashValueNull -> when(second) {
            is KrashValueNull -> false
            else -> throw KrashRuntimeException("Invalid type for inequality comparison with null!")
        }

        // Compare Object
        is KrashValueObject -> when(second) {
            is KrashValueObject -> !first.isEqual(second)
            else -> throw KrashRuntimeException("Invalid type for equality comparison with object!")
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

class KrashExpressionConditionIs(private val first: KrashExpression, private val second: String): KrashExpression() {

    override fun toValue(runtime: KrashRuntime) = KrashValueBoolean(first.toValue(runtime).let {value ->

        // Resolve Type
        second.let {type -> when {

            // Native Class
            KrashValueClass.nativeContains(type) -> value is KrashValueObject && value.getClass().getName() == type

            // Defined Class
            KrashRuntime.classExists(type) -> value is KrashValueObject && value.getClass().getName() == type

            // Defined Enum
            KrashRuntime.enumExists(type) -> value is KrashValueEnum && value.getName() == type

            // Defined Method
            KrashRuntime.methodExists(type) -> value is KrashValueCallable && value.getName() == type

            // Simple Type
            else -> when(type) {
                "array" -> value is KrashValueArray
                "boolean" -> value is KrashValueBoolean
                "callable" -> value is KrashValueCallable
                //"class" -> value is KrashValueClass
                "double" -> value is KrashValueDouble
                //"enum" -> value is KrashValueEnum
                "integer" -> value is KrashValueInteger
                "map" -> value is KrashValueMap
                "null" -> value is KrashValueNull
                "object" -> value is KrashValueObject
                "string" -> value is KrashValueString
                else -> throw KrashRuntimeException("Invalid type '$type' for is operator!")
            }
            // NOTE: omitting class and enum due to parser issues
        }}
    })

}

class KrashExpressionConditionLesserEqual(first: KrashExpression, second: KrashExpression): KrashExpressionCondition(first, second) {

    override fun invoke(runtime: KrashRuntime, first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Compare Numbers
        is KrashValueSimpleNumeric -> when(second) {
            is KrashValueSimpleNumeric -> first.toDouble() <= second.toDouble()
            else -> throw KrashRuntimeException("Invalid type for lesser than or equal tp comparison with number!")
        }

        // Invalid Type
        else -> throw KrashRuntimeException("Invalid type for lesser than comparison!")
    }

}

class KrashExpressionConditionLesserThan(first: KrashExpression, second: KrashExpression): KrashExpressionCondition(first, second) {

    override fun invoke(runtime: KrashRuntime, first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Compare Numbers
        is KrashValueSimpleNumeric -> when(second) {
            is KrashValueSimpleNumeric -> first.toDouble() < second.toDouble()
            else -> throw KrashRuntimeException("Invalid type for lesser than comparison with number!")
        }

        // Invalid Type
        else -> throw KrashRuntimeException("Invalid type for lesser than comparison!")
    }

}