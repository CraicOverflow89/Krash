package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.objects.KrashValueInteger
import craicoverflow89.krash.components.objects.KrashValueSimple
import craicoverflow89.krash.components.objects.KrashValueString

abstract class KrashExpressionOperator(private val first: KrashExpression, private val second: KrashExpression): KrashExpression() {

    abstract fun invoke(first: KrashValueSimple, second: KrashValueSimple): KrashValueSimple

    override fun toValue(runtime: KrashRuntime) = invoke(first.toValue(runtime), second.toValue(runtime))

}

class KrashExpressionOperatorAddition(first: KrashExpression, second: KrashExpression): KrashExpressionOperator(first, second) {

    override fun invoke(first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Integer Addition
        is KrashValueInteger -> {

            // Perform Addition
            if(second is KrashValueInteger) KrashValueInteger(first.value + second.value)

            // Invalid Type
            else throw RuntimeException("Invalid type to perform addition!")
        }

        // String Concatenation
        is KrashValueString -> {

            // Perform Concatenation
            if(second is KrashValueString) KrashValueString(first.value + second.value)

            // Invalid Type
            else throw RuntimeException("Invalid type to perform concatenation!")
        }

        // Invalid Type
        else -> throw RuntimeException("Invalid type to perform operator!")
    }

}

class KrashExpressionOperatorDivision(first: KrashExpression, second: KrashExpression): KrashExpressionOperator(first, second) {

    override fun invoke(first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Integer Division
        is KrashValueInteger -> {

            // Perform Division
            if(second is KrashValueInteger) {

                // Zero Safety
                if(second.value == 0) throw RuntimeException("Cannot perform division by zero!")

                // Perform Division
                KrashValueInteger(first.value / second.value)
            }

            // Invalid Type
            else throw RuntimeException("Invalid type to perform division!")
        }

        // Invalid Type
        else -> throw RuntimeException("Invalid type to perform operator!")
    }

}

class KrashExpressionOperatorMultiplication(first: KrashExpression, second: KrashExpression): KrashExpressionOperator(first, second) {

    override fun invoke(first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Integer Multiplication
        is KrashValueInteger -> {

            // Perform Multiplication
            if(second is KrashValueInteger) KrashValueInteger(first.value * second.value)

            // Invalid Type
            else throw RuntimeException("Invalid type to perform multiplication!")
        }

        // Invalid Type
        else -> throw RuntimeException("Invalid type to perform operator!")
    }

}

class KrashExpressionOperatorSubtraction(first: KrashExpression, second: KrashExpression): KrashExpressionOperator(first, second) {

    override fun invoke(first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Integer Subtraction
        is KrashValueInteger -> {

            // Perform Subtraction
            if(second is KrashValueInteger) KrashValueInteger(first.value - second.value)

            // Invalid Type
            else throw RuntimeException("Invalid type to perform subtraction!")
        }

        // Invalid Type
        else -> throw RuntimeException("Invalid type to perform operator!")
    }

}