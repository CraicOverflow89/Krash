package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashRuntimeException
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
            else -> throw KrashRuntimeException("Invalid type to perform addition!")
        }

        // String Concatenation
        is KrashValueString -> when(second) {

            // Perform Concatenation
            is KrashValueString -> KrashValueString(first.getValue() + second.getValue())

            // Invalid Type
            else -> throw KrashRuntimeException("Invalid type to perform addition!")
        }

        // Invalid Type
        else -> throw KrashRuntimeException("Invalid type to perform operator!")
    }

}

class KrashExpressionOperatorDivision(first: KrashExpression, second: KrashExpression): KrashExpressionOperator(first, second) {

    override fun invoke(first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Numeric Division
        is KrashValueSimpleNumeric -> when(second) {

            // Perform Division
            is KrashValueSimpleNumeric -> {

                // Zero Safety
                if(second.toDouble() == 0.0) throw KrashRuntimeException("Cannot perform division by zero!")

                // Perform Division
                KrashValueSimpleNumeric.create(first.toDouble() / second.toDouble())
            }

            // Invalid Type
            else -> throw KrashRuntimeException("Invalid type to perform division!")
        }

        // Invalid Type
        else -> throw KrashRuntimeException("Invalid type to perform operator!")
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
                                if(index is KrashValueInteger) {

                                    // TEMP DEBUG
                                    println("KrashExpressionOperatorIncrement updating array")
                                    println(" collection is $collection")
                                    println(" index is ${index.value}")

                                    // NOTE: using setElement on collection is working fine
                                    //       however it's the root collection that needs to be committed to heap
                                    //       so this setElement (then finally heapPut) logic needs to cascade through parents
                                    //       all of this logic (see also KrashCommandDeclareReferenceIndex) needs to be moved
                                    //       into a single method for updating collection values and committing to heap

                                    // Update Heap
                                    runtime.heapPut(ref.getRef(runtime).value, collection.apply {

                                        // Update Collection
                                        collection.setElement(index.value, result)
                                    })
                                }

                                // Invalid Type
                                else throw KrashRuntimeException("Array indexes must be integers!")
                            }

                            // Update Map
                            is KrashValueMap -> {

                                // String Key
                                if(index is KrashValueString) {

                                    // Update Heap
                                    runtime.heapPut(ref.getRef(runtime).value, collection.apply {

                                        // Update Map
                                        setData(index.getValue(), result)
                                    })
                                }

                                // Invalid Type
                                else throw KrashRuntimeException("Map indexes must be strings!")
                            }

                            // Invalid Type
                            else -> throw KrashRuntimeException("Cannot append to a non-indexable value!")
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
        else throw KrashRuntimeException("Invalid type to perform operator!")
    }

}

class KrashExpressionOperatorIncrementIndex(private val value: KrashExpression, private val index: KrashExpression): KrashExpressionOperatorIncrementValue {

    fun getCollection(runtime: KrashRuntime) = value.toValue(runtime)

    fun getIndex(runtime: KrashRuntime) = index.toValue(runtime)

    fun getRef(runtime: KrashRuntime): KrashValueReference = value.toValueRef(runtime).let {
        // TEMP
        if(it !is KrashValueReference) throw KrashRuntimeException("Could not resolve to a reference for this value!")
        it
    }

    fun getValue(runtime: KrashRuntime) = value.toValue(runtime)

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
            else -> throw KrashRuntimeException("Invalid type to perform multiplication!")
        }

        // Invalid Type
        else -> throw KrashRuntimeException("Invalid type to perform operator!")
    }

}

class KrashExpressionOperatorNegation(private val value: KrashExpression): KrashExpression() {

    override fun toValue(runtime: KrashRuntime) = value.toValue(runtime).let {
        when(it) {

            // Boolean Negation
            is KrashValueBoolean -> KrashValueBoolean(!it.isTrue())

            // Invalid Type
            else -> throw KrashRuntimeException("Invalid type to perform operator!")
        }
    }

}

class KrashExpressionOperatorSubtraction(first: KrashExpression, second: KrashExpression): KrashExpressionOperator(first, second) {

    override fun invoke(first: KrashValueSimple, second: KrashValueSimple) = when(first) {

        // Numeric Subtraction
        is KrashValueSimpleNumeric -> when(second) {

            // Perform Subtraction
            is KrashValueSimpleNumeric -> KrashValueSimpleNumeric.create(first.toDouble() - second.toDouble())

            // Invalid Type
            else -> throw KrashRuntimeException("Invalid type to perform subtraction!")
        }

        // Invalid Type
        else -> throw KrashRuntimeException("Invalid type to perform operator!")
    }

}