package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashException
import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.objects.KrashValue
import craicoverflow89.krash.components.objects.KrashValueArray
import craicoverflow89.krash.components.objects.KrashValueCallable
import craicoverflow89.krash.components.objects.KrashValueClass
import craicoverflow89.krash.components.objects.KrashValueInteger
import craicoverflow89.krash.components.objects.KrashValueMap
import craicoverflow89.krash.components.objects.KrashValueReference
import craicoverflow89.krash.components.objects.KrashValueSimple
import craicoverflow89.krash.components.objects.KrashValueString

abstract class KrashExpression {

    abstract fun toValue(runtime: KrashRuntime): KrashValueSimple

    open fun toValueRef(runtime: KrashRuntime): KrashValue {

        // Default Behaviour
        return toValue(runtime)
    }

}

class KrashExpressionData(private val value: KrashExpression, private val toString: Boolean): KrashExpression() {

    override fun toValue(runtime: KrashRuntime) = value.toValue(runtime).let {
        if(toString) KrashValueString(it.toString()) else it
    }

}

class KrashExpressionGlobal(private val value: String): KrashExpression() {

    override fun toValue(runtime: KrashRuntime) = KrashRuntime.global(value)

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
                    else throw KrashException("Array indexes must be integers!")
                }

                // Map Key
                is KrashValueMap -> {

                    // String Key
                    if(index is KrashValueString) value.getData(index.value)

                    // Invalid Type
                    else throw KrashException("Map indexes must be strings!")
                }

                // String Character
                is KrashValueString -> {

                    // Integer Position
                    if(index is KrashValueInteger) value.getChar(index.value)

                    // Invalid Type
                    else throw KrashException("Character indexes must be integers!")
                }

                // Invalid Type
                else -> throw KrashException("Cannot access index $index of this value!")
            }.toSimple(runtime)
        }
    }

}

class KrashExpressionInvoke(private val value: KrashExpression, private val argumentList: List<KrashExpression>): KrashExpression() {

    override fun toValue(runtime: KrashRuntime) = value.toValue(runtime).let {
        when(it) {

            // Invoke Callable
            is KrashValueCallable -> it.invoke(runtime, argumentList.map {
                it.toValue(runtime)
            }).toSimple(runtime)

            // Invoke Constructor
            is KrashValueClass -> it.create(runtime, argumentList.map {
                it.toValue(runtime)
            })

            // Invalid Type
            else -> throw KrashException("Could not invoke this non-callable value!")
        }
    }

}

class KrashExpressionMember(private val value: KrashExpression, private val member: String): KrashExpression() {

    override fun toValue(runtime: KrashRuntime): KrashValueSimple = value.toValue(runtime).let {

        // Access Member
        if(it is KrashValueSimple) return it.memberGet(member).toSimple(runtime)

        // Invalid Type
        else throw KrashException("Cannot access members for this value!")
    }

}

class KrashExpressionReference(private val value: String, private val byRef: Boolean): KrashExpression() {

    override fun toValue(runtime: KrashRuntime) = KrashValueReference(value, byRef).toSimple(runtime)

    override fun toValueRef(runtime: KrashRuntime) = KrashValueReference(value, byRef)

}