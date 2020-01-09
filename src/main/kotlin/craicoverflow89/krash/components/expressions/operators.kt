package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashException
import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.objects.KrashValueReference
import craicoverflow89.krash.components.objects.KrashValueSimple
import craicoverflow89.krash.components.objects.KrashValueSimpleNumeric
import craicoverflow89.krash.components.objects.KrashValueString

abstract class KrashExpressionOperator(private val first: KrashExpression, private val second: KrashExpression): KrashExpression() {

    abstract fun invoke(first: KrashValueSimple, second: KrashValueSimple): KrashValueSimple

    override fun toValue(runtime: KrashRuntime) = invoke(first.toValue(runtime), second.toValue(runtime))

}

class KrashExpressionOperatorAddition(first: KrashExpression, second: KrashExpression): KrashExpressionOperator(first, second) {

    override fun invoke(first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Numeric Addition
        is KrashValueSimpleNumeric -> when(second) {

            // Perform Addition
            is KrashValueSimpleNumeric -> KrashValueSimpleNumeric.create(first.toDouble() + second.toDouble())

            // Invalid Type
            else -> throw KrashException("Invalid type to perform addition!")
        }

        // String Concatenation
        is KrashValueString -> when(second) {

            // Perform Concatenation
            is KrashValueString -> KrashValueString(first.value + second.value)

            // Invalid Type
            else -> throw KrashException("Invalid type to perform addition!")
        }

        // Invalid Type
        else -> throw KrashException("Invalid type to perform operator!")
    }

}

class KrashExpressionOperatorDivision(first: KrashExpression, second: KrashExpression): KrashExpressionOperator(first, second) {

    override fun invoke(first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Numeric Division
        is KrashValueSimpleNumeric -> when(second) {

            // Perform Division
            is KrashValueSimpleNumeric -> {

                // Zero Safety
                if(second.toDouble() == 0.0) throw KrashException("Cannot perform division by zero!")

                // Perform Division
                KrashValueSimpleNumeric.create(first.toDouble() / second.toDouble())
            }

            // Invalid Type
            else -> throw KrashException("Invalid type to perform division!")
        }

        // Invalid Type
        else -> throw KrashException("Invalid type to perform operator!")
    }

}

class KrashExpressionOperatorIncrement(private val ref: KrashExpressionOperatorIncrementValue, private val type: KrashExpressionOperatorIncrementType): KrashExpression() {

    private fun invoke(runtime: KrashRuntime, value: KrashValueSimpleNumeric): KrashValueSimple {

        // Increment Value
        runtime.heapPut(ref.getValue(), KrashValueSimpleNumeric.create(when(type) {
            KrashExpressionOperatorIncrementType.MINUS -> value.toDouble() - 1
            KrashExpressionOperatorIncrementType.PLUS -> value.toDouble() + 1
        }))
        // NOTE: this only works for x ++
        //       when it's x[y][z] it needs to update the list or map

        // Return Result
        return value
    }

    override fun toValue(runtime: KrashRuntime): KrashValueSimple {

        // Resolve Expression
        val value = ref.toValue(runtime)

        // Increment Value
        if(value is KrashValueSimpleNumeric) return invoke(runtime, value)

        // Invalid Type
        else throw KrashException("Invalid type to perform operator!")
    }

}

class KrashExpressionOperatorIncrementIndex(private val ref: KrashExpressionOperatorIncrementValue, private val index: KrashExpression): KrashExpressionOperatorIncrementValue {

    override fun getValue() = ref.getValue()

    override fun toValue(runtime: KrashRuntime) = index.toValue(runtime)

}

class KrashExpressionOperatorIncrementReference(private val ref: String): KrashExpressionOperatorIncrementValue {

    override fun getValue() = ref

    override fun toValue(runtime: KrashRuntime) = runtime.heapGet(ref).toSimple(runtime)
    // NOTE: this will need to be updated for reference persistence

}

enum class KrashExpressionOperatorIncrementType {
    MINUS, PLUS
}

interface KrashExpressionOperatorIncrementValue {

    fun getValue(): String

    fun index(index: KrashExpression) = KrashExpressionOperatorIncrementIndex(this, index)

    fun toValue(runtime: KrashRuntime): KrashValueSimple

}

class KrashExpressionOperatorMultiplication(first: KrashExpression, second: KrashExpression): KrashExpressionOperator(first, second) {

    override fun invoke(first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Numeric Multiplication
        is KrashValueSimpleNumeric -> when(second) {

            // Perform Multiplication
            is KrashValueSimpleNumeric -> KrashValueSimpleNumeric.create(first.toDouble() * second.toDouble())

            // Invalid Type
            else -> throw KrashException("Invalid type to perform multiplication!")
        }

        // Invalid Type
        else -> throw KrashException("Invalid type to perform operator!")
    }

}

class KrashExpressionOperatorSubtraction(first: KrashExpression, second: KrashExpression): KrashExpressionOperator(first, second) {

    override fun invoke(first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Numeric Subtraction
        is KrashValueSimpleNumeric -> when(second) {

            // Perform Subtraction
            is KrashValueSimpleNumeric -> KrashValueSimpleNumeric.create(first.toDouble() - second.toDouble())

            // Invalid Type
            else -> throw KrashException("Invalid type to perform subtraction!")
        }

        // Invalid Type
        else -> throw KrashException("Invalid type to perform operator!")
    }

}