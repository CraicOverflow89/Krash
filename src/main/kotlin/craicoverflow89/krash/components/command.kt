package craicoverflow89.krash.components

import craicoverflow89.krash.components.expressions.KrashExpression
import craicoverflow89.krash.components.expressions.KrashExpressionLiteralCallableArgument
import craicoverflow89.krash.components.objects.*

interface KrashCommand {

    fun invoke(runtime: KrashRuntime): KrashValue

}

class KrashCommandComment(private val value: String): KrashCommand {

    fun getValue() = value

    override fun invoke(runtime: KrashRuntime) = KrashValueNull()

}

class KrashCommandDeclare(private val ref: KrashCommandDeclareReference, private val value: KrashExpression): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue {

        // Resolve Value
        val valueSimple = value.toValue(runtime)

        // Resolve Reference
        when(ref) {

            // Indexed Reference
            is KrashCommandDeclareReferenceIndex -> {

                // Resolve Index
                ref.toValue(runtime).let {

                    // Update Value
                    when(it.value) {

                        // Update Array
                        is KrashValueArray -> {

                            // Integer Position
                            if(it.index is KrashValueInteger) it.value.setElement(it.index.value, valueSimple)

                            // Invalid Type
                            else throw KrashRuntimeException("Array indexes must be integers!")
                        }

                        // Update Map
                        is KrashValueMap -> {

                            // String Key
                            if(it.index is KrashValueString) it.value.setData(it.index.getValue(), valueSimple)

                            // Invalid Type
                            else throw KrashRuntimeException("Map indexes must be strings!")
                        }

                        // Invalid Type
                        else -> throw KrashRuntimeException("Cannot append to a non-indexable value!")
                    }
                }
            }

            // Simple Reference
            is KrashCommandDeclareReferenceSimple -> ref.value.let {

                // Reserved Term
                if(KrashReserved.contains(it)) {
                    throw KrashRuntimeException("Reserved term '$it' is not a valid reference!")
                }

                // Update Heap
                runtime.heapPut(it, valueSimple)
            }
        }

        // Done
        return valueSimple
    }

}

abstract class KrashCommandDeclareReference

class KrashCommandDeclareReferenceIndex(private val value: KrashExpression, private val index: KrashExpression): KrashCommandDeclareReference() {

    fun toValue(runtime: KrashRuntime) = KrashCommandDeclareReferenceIndexResult(value.toValue(runtime), index.toValue(runtime))

}

class KrashCommandDeclareReferenceIndexResult(val value: KrashValueSimple, val index: KrashValueSimple)

class KrashCommandDeclareReferenceSimple(val value: String): KrashCommandDeclareReference()

class KrashCommandEnum(private val name: String, private val valueList: List<String>): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue {

        // Register Enum
        KrashRuntime.enumRegister(name, KrashValueEnum(name, valueList))

        // Done
        return KrashValueNull()
    }

}

class KrashCommandExpression(private val value: KrashExpression): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue = value.toValue(runtime)

}

class KrashCommandFunction(private val name: String, private val argumentList: List<KrashExpressionLiteralCallableArgument>, private val commandList: List<KrashCommand>): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue {

        // Register Function
        KrashRuntime.methodRegister(name, KrashValueCallable.create(runtime, name, argumentList, commandList))

        // Done
        return KrashValueNull()
    }

}

class KrashCommandKeyword(private val type: KrashCommandKeywordType): KrashCommand {

    override fun invoke(runtime: KrashRuntime): KrashValue {

        // Invoke Listener
        if(runtime.keywordListenerInvoke(type)) return KrashValueNull()

        // Invalid Keyword
        throw KrashRuntimeException("Invalid keyword '${type.name.toLowerCase()}' encountered!")
        // NOTE: this should probably NOT be a runtime exception
    }

}

enum class KrashCommandKeywordType {
    BREAK, CONTINUE
}