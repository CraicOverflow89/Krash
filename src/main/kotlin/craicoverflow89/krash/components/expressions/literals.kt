package craicoverflow89.krash.components.expressions

import craicoverflow89.krash.components.KrashCommand
import craicoverflow89.krash.components.KrashCommandComment
import craicoverflow89.krash.components.KrashCommandDeclareReferenceSimple
import craicoverflow89.krash.components.KrashCommandFunction
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

class KrashExpressionLiteralCallable(private val argumentList: List<KrashExpressionLiteralCallableArgument>, private val commandList: List<KrashCommand>): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime) = KrashValueCallable.create(runtime, argumentList, commandList)

}

class KrashExpressionLiteralCallableArgument(val name: String, private val defaultValue: KrashExpression? = null, val modifier: KrashExpressionLiteralCallableArgumentModifier) {

    fun defaultValue(runtime: KrashRuntime) = defaultValue?.toValue(runtime) ?: KrashValueNull()

}

enum class KrashExpressionLiteralCallableArgumentModifier {
    NONE, REF, STRING
}

class KrashExpressionLiteralClass(private val name: String, private val modifier: KrashValueClassModifier, private val argumentList: List<KrashExpressionLiteralCallableArgument>, private val inherit: KrashExpressionLiteralClassInherit?, private val expressionList: List<KrashExpressionLiteralClassExpression>): KrashExpressionLiteral() {

    override fun toValue(runtime: KrashRuntime): KrashValueSimple {

        // Validate Inheritance
        inherit?.validate()

        // Create Heap
        val classRuntime = runtime.child()
        val classArgs = argumentList

        // Create Class
        return KrashValueClass(name, classRuntime, modifier, argumentList, inherit?.parentClass(), inherit?.argumentTransfer()) {runtime, argumentList ->

            // NOTE: why is argumentList not being used?
            //       have moved classRuntime population elsewhere but still need to use these

            // Custom Constructor
            // NOTE: invoke if there has been a constructor method defined in the class
            //       it will use classRuntime for heap access

            // Return Members
            expressionList.map {
                it.resolve(classRuntime)
            }.filter {
                !it.isAnon()
            }
        }.apply {

            // Register Class
            KrashRuntime.classRegister(name, this)
        }
    }

}

abstract class KrashExpressionLiteralClassExpression: KrashExpressionLiteral() {

    abstract fun resolve(runtime: KrashRuntime): KrashValueClassMember

    override fun toValue(runtime: KrashRuntime) = resolve(runtime)

}

class KrashExpressionLiteralClassExpressionComment(private val command: KrashCommandComment): KrashExpressionLiteralClassExpression() {

    override fun resolve(runtime: KrashRuntime) = KrashValueClassMember.anon(KrashValueNull())

}

class KrashExpressionLiteralClassExpressionMethod(private val name: String, private val value: KrashExpressionLiteralCallable): KrashExpressionLiteralClassExpression() {

    override fun resolve(runtime: KrashRuntime) = KrashValueClassMember(name, value.toValue(runtime))

}

class KrashExpressionLiteralClassExpressionProperty(private val name: String, private val value: KrashExpression): KrashExpressionLiteralClassExpression() {

    override fun resolve(runtime: KrashRuntime) = KrashValueClassMember(name, value.toValue(runtime))

}

class KrashExpressionLiteralClassInherit(private val name: String, private val args: List<KrashExpression>) {

    fun argumentTransfer() = args

    fun parentClass(): KrashValueClass {

        // Invalid Name
        if(!KrashRuntime.classExists(name)) throw KrashRuntimeException("Could not find '$name' super class!")

        // Return Class
        return KrashRuntime.classGet(name)
    }

    fun validate() {

        // Super Class
        parentClass().let {

            // Final Class
            if(it.isFinal()) throw KrashRuntimeException("Cannot extend final '${it.getName()}' class!")
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