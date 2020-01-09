package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashException
import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.objects.*

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
        val result = KrashValueSimpleNumeric.create(when(type) {
            KrashExpressionOperatorIncrementType.MINUS -> value.toDouble() - 1
            KrashExpressionOperatorIncrementType.PLUS -> value.toDouble() + 1
        })

        // Update Reference
        when(ref) {

            // Indexed Reference
            is KrashExpressionOperatorIncrementIndex -> {

                /*println("KrashExpressionOperatorIncrement.invoke")
                println(" updating an index")
                println(" value is $value")
                println(" collection is ${ref.getCollection(runtime)}")
                println("")*/

                // Update Collection
                ref.getCollection(runtime).let {collection ->

                    // Resolve Index
                    ref.getIndex(runtime).let {index ->
                        when(collection) {

                            // Update Array
                            is KrashValueArray -> {

                                // Integer Position
                                //if(index is KrashValueInteger) collection.setElement(index.value, result)

                                // TEMP DEBUG
                                if(index is KrashValueInteger) {
                                    collection.setElement(index.value, result)
                                    println("updating array collection")
                                    println(collection)
                                    println(collection.getElement(index.value))
                                    println("")
                                }

                                // Invalid Type
                                else throw KrashException("Array indexes must be integers!")
                            }

                            // Update Map
                            is KrashValueMap -> {

                                // String Key
                                if(index is KrashValueString) collection.setData(index.value, result)

                                // Invalid Type
                                else throw KrashException("Map indexes must be strings!")
                            }

                            // Invalid Type
                            else -> throw KrashException("Cannot append to a non-indexable value!")
                        }
                    }
                }
            }

            // Simple Reference
            is KrashExpressionOperatorIncrementReference -> runtime.heapPut(ref.value, result)
        }

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

class KrashExpressionOperatorIncrementIndex(private val value: KrashExpression, private val index: KrashExpression): KrashExpressionOperatorIncrementValue {

    fun getCollection(runtime: KrashRuntime) = value.toValue(runtime)

    fun getIndex(runtime: KrashRuntime) = index.toValue(runtime)

    override fun toValue(runtime: KrashRuntime) = KrashExpressionIndex(value, index).toValue(runtime)

}

class KrashExpressionOperatorIncrementReference(val value: String): KrashExpressionOperatorIncrementValue {

    override fun toValue(runtime: KrashRuntime) = runtime.heapGet(value).toSimple(runtime)
    // NOTE: this will need to be updated for reference persistence

}

enum class KrashExpressionOperatorIncrementType {
    MINUS, PLUS
}

interface KrashExpressionOperatorIncrementValue {

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