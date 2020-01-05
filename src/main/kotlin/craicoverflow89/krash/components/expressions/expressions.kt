package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.KrashValue
import craicoverflow89.krash.components.KrashValueArray
import craicoverflow89.krash.components.KrashValueCallable
import craicoverflow89.krash.components.KrashValueInteger
import craicoverflow89.krash.components.KrashValueMap
import craicoverflow89.krash.components.KrashValueReference
import craicoverflow89.krash.components.KrashValueSimple
import craicoverflow89.krash.components.KrashValueString
// NOTE: should be able to eliminate the concept of KrashValueSimple altogether
//       when all resolution stuff is handled in expressions

abstract class KrashExpression {

    abstract fun toValue(runtime: KrashRuntime): KrashValueSimple

}

class KrashExpressionIndex(private val value: KrashExpression, private val index: KrashExpression): KrashExpression() {

    override fun toValue(runtime: KrashRuntime) = value.toValue(runtime).let {value ->
        index.toValue(runtime).let {index ->
            when(value) {

                // Array Element
                is KrashValueArray -> {

                    // Integer Position
                    if(index is KrashValueInteger) value.getElement(index.value)

                    // Invalid Type
                    else throw RuntimeException("Array indexes must be integers!")
                    // NOTE: this is where custom exception handling should be added
                }

                // Map Key
                is KrashValueMap -> {

                    // String Key
                    if(index is KrashValueString) value.getData(index.value)

                    // Invalid Type
                    else throw RuntimeException("Map indexes must be strings!")
                    // NOTE: this is where custom exception handling should be added
                }

                // String Character
                is KrashValueString -> {

                    // Integer Position
                    if(index is KrashValueInteger) value.getChar(index.value)

                    // Invalid Type
                    else throw RuntimeException("Character indexes must be integers!")
                    // NOTE: come back to this; use custom exceptions later
                }

                // Invalid Type
                else -> throw RuntimeException("Cannot access index $index of this value!")
                // NOTE: come back to this; use custom exceptions later
            }.toSimple(runtime)
        }
    }

}

class KrashExpressionInvoke(private val value: KrashExpression, private val argumentList: List<KrashExpression>): KrashExpression() {

    override fun toValue(runtime: KrashRuntime) = value.toValue(runtime).let {

        // Invoke Callable
        if(it is KrashValueCallable) it.invoke(runtime, argumentList.map {
            it.toValue(runtime)
        }).toSimple(runtime)

        // Invalid Type
        else throw RuntimeException("Could not invoke this non-callable value!")
        // NOTE: come back to this; use custom exceptions later
    }

}

class KrashExpressionMember(private val value: KrashExpression, private val member: String): KrashExpression() {

    override fun toValue(runtime: KrashRuntime): KrashValueSimple = value.toValue(runtime).let {

        // Access Member
        if(it is KrashValueSimple) return it.memberGet(member).toSimple(runtime)
        // NOTE: should be able to eliminate the concept of KrashValueSimple altogether
        //       when all resolution stuff is handled in expressions

        // Invalid Type
        else throw RuntimeException("Cannot access members for this value!")
        // NOTE: come back to this; use custom exceptions later
    }

}

class KrashExpressionReference(private val value: String, private val byRef: Boolean): KrashExpression() {

    override fun toValue(runtime: KrashRuntime) = KrashValueReference(value, byRef).toSimple(runtime)
    // NOTE: this really isn't suitable (will need byRef at times)

}