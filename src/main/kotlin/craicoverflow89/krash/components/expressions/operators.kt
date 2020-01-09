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

class KrashExpressionOperatorDecrement(private val value: KrashExpression): KrashExpression() {

    private fun invoke(runtime: KrashRuntime, value: KrashValueReference): KrashValueSimple = value.toSimple(runtime).let {
        when(it) {

            // Numeric Decrement
            is KrashValueSimpleNumeric -> {

                // Decrement Value
                runtime.heapPut(value.value, KrashValueSimpleNumeric.create(it.toDouble() - 1))

                // Return Original
                it
            }

            // Invalid Type
            else -> throw KrashException("Invalid type to perform decrement!")
        }
    }

    override fun toValue(runtime: KrashRuntime): KrashValueSimple {

        // Reference Expression
        if(value is KrashExpressionReference) return invoke(runtime, value.toValueRef(runtime))

        // Invalid Type
        else throw KrashException("Invalid type to perform operator!")
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

class KrashExpressionOperatorIncrement(private val value: KrashExpression): KrashExpression() {

    private fun invoke(runtime: KrashRuntime, value: KrashValueReference): KrashValueSimple = value.toSimple(runtime).let {
        when(it) {

            // Numeric Increment
            is KrashValueSimpleNumeric -> {

                // Increment Value
                runtime.heapPut(value.value, KrashValueSimpleNumeric.create(it.toDouble() + 1))

                // Return Original
                it
            }

            // Invalid Type
            else -> throw KrashException("Invalid type to perform increment!")
        }
    }

    override fun toValue(runtime: KrashRuntime): KrashValueSimple {

        // Resolve Expression
        val valueRef = value.toValueRef(runtime)

        // TEMP DEBUG
        println("KrashExpressionOperatorIncrement")
        println(value)
        println(valueRef)
        println()
        // NOTE: need a new parser rule for pre/post inc/dec operations
        //       PLUS PLUS ref (index)* -> value after change
        //       ref (index)* PLUS PLUS -> value before change

        // Reference Value
        if(valueRef is KrashValueReference) return invoke(runtime, valueRef)

        // Invalid Type
        else throw KrashException("Invalid type to perform operator!")
    }

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