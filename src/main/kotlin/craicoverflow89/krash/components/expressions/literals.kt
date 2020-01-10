package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashCommand
import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.KrashRuntimeException
import craicoverflow89.krash.components.objects.*

abstract class KrashExpressionLiteral: KrashExpression()

class KrashExpressionLiteralArray(private val value: List<KrashExpression>): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueArray(ArrayList<KrashValue>().apply {
        value.forEach {
            add(it.toValue(runtime))
        }
    })

}

class KrashExpressionLiteralBoolean(private val value: Boolean): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueBoolean(value)

}

class KrashExpressionLiteralCallable(private val argumentList: List<KrashExpressionLiteralCallableArgument>, private val expressionList: List<KrashExpressionLiteralCallableExpression>): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime): KrashValueCallable {

        // Create Heap
        val callableRuntime = runtime.child()
        val callableArgs = argumentList

        // Create Callable
        return KrashValueCallable {_: KrashRuntime, argumentList: List<KrashValue> ->

            // Inject Arguments
            callableArgs.forEachIndexed {pos, arg ->
                callableRuntime.heapPut(arg.name, argumentList.let {

                    // Argument Value
                    if(pos < argumentList.size) argumentList[pos].let {

                        // Cast String
                        if(arg.modifier == KrashExpressionLiteralCallableArgumentModifier.STRING) it.toStringType()

                        // Keep Value
                        else it
                    }

                    // Default Value
                    else arg.defaultValue(runtime)
                })
            }

            // Implicit It
            if(argumentList.size == 1) callableRuntime.heapPut("it", argumentList[0])

            // Default Result
            var returnValue: KrashValue = KrashValueNull()

            // Iterate Expressions
            var result: KrashValue
            var pos = 0
            while(pos < expressionList.size) {

                // Invoke Expression
                result = expressionList[pos].toValue(callableRuntime)

                // Return Result
                if(expressionList[pos].isReturn) {
                    returnValue = result
                    break
                }

                // Next Expression
                pos ++
            }

            // Return Result
            returnValue
        }
    }

}

class KrashExpressionLiteralCallableArgument(val name: String, private val defaultValue: KrashExpression? = null, val modifier: KrashExpressionLiteralCallableArgumentModifier) {

    fun defaultValue(runtime: KrashRuntime) = defaultValue?.toValue(runtime) ?: KrashValueNull()

}

enum class KrashExpressionLiteralCallableArgumentModifier {
    NONE, REF, STRING
}

class KrashExpressionLiteralCallableExpression(private val command: KrashCommand, val isReturn: Boolean): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = command.invoke(runtime).toSimple(runtime)

}

class KrashExpressionLiteralClass(private val name: String, private val argumentList: List<KrashExpressionLiteralCallableArgument>, private val inherit: KrashExpressionLiteralClassInherit?, private val expressionList: List<KrashExpressionLiteralClassExpression>): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime): KrashValueSimple {

        // Validate Inheritance
        inherit?.validate()
        //       need to ensure that arguments supplied satisfy super constructor requirements

        // Create Heap
        val classRuntime = runtime.child()
        val classArgs = argumentList

        // NOTE: need to process the expressionList using classRuntime to store created values

        // Create Class
        return KrashValueClass(name) {runtime, argumentList ->

            // NOTE: Validate argumentList (provided) against classArgs (expected)

            // Super Constructor
            // inherit?.something ??
            // NOTE: will need to merge members of parent with members being created here
            //       or maybe that logic should exist where this init callable is being invoked

            // Return Members
            hashMapOf(
                Pair("name", KrashValueString(name))
            )
            // NOTE: perhaps the above should live in an immutable member 'class'
            //       class will return a KrashObject that represents the class structure
            //       the values created with expressions need to be accessible as members
        }.apply {

            // Register Class
            KrashRuntime.classRegister(name, this)
        }
    }

}

class KrashExpressionLiteralClassExpression(private val command: KrashCommand): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = command.invoke(runtime).toSimple(runtime)

}

class KrashExpressionLiteralClassInherit(private val name: String, private val args: List<KrashExpression>) {

    fun validate() {

        // Invalid Name
        if(!KrashRuntime.classExists(name)) throw KrashRuntimeException("Could not find '$name' super class!")

        // Super Class
        KrashRuntime.classGet(name).let {

            // Validate Arguments
            // NOTE: compare args (supplied) with expected (it.?) arguments
        }
    }

}

class KrashExpressionLiteralDouble(private val value: Double): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueDouble(value)

}

class KrashExpressionLiteralInteger(private val value: Int): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueInteger(value)

}

class KrashExpressionLiteralMap(private val value: List<KrashExpressionLiteralMapPair>): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueMap(value.map {
        it.toValue(runtime)
    })

}

class KrashExpressionLiteralMapPair(private val key: String, private val value: KrashExpression) {

    fun toValue(runtime: KrashRuntime) = KrashValueMapPair(key, value.toValue(runtime))

}

class KrashExpressionLiteralNull: KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueNull()

}

class KrashExpressionLiteralString(private val value: String): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueString(value.let {

        // Complex Value
        val pattern = Regex("\\$[A-Za-z_][A-Za-z0-9_]*")
        if(it.contains(pattern)) {

            // Resolution Logic
            fun resolve(text: String, match: MatchResult): String {

                // Update String
                val result = match.value.substring(1).let {ref ->
                    text.replace(match.value, ref.let {

                        // Global Value
                        if(KrashRuntime.globalContains(it)) KrashRuntime.global(it).toString()

                        // Lookup Reference
                        else KrashValueReference(ref, false).resolve(runtime).toSimple(runtime).toString()
                    })
                }

                // Match All
                return match.next()?.let {
                    resolve(result, it)
                } ?: result
            }

            // Match References
            resolve(it, pattern.find(it)!!)
        }

        // Simple Value
        else it
    })

}