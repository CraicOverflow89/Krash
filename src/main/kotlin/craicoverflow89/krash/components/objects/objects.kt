package craicoverflow89.krash.components.objects

import craicoverflow89.krash.components.KrashRuntimeException
import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.expressions.KrashExpression
import craicoverflow89.krash.components.expressions.KrashExpressionLiteralCallableArgument
import craicoverflow89.krash.components.expressions.KrashExpressionLiteralCallableArgumentModifier
import craicoverflow89.krash.system.KrashFileSystem
import craicoverflow89.krash.system.KrashServer
import craicoverflow89.krash.system.KrashServerRequest
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

open class KrashValueClass(private val name: String, private val classRuntime: KrashRuntime?, private val modifier: KrashValueClassModifier, private val constructorList: List<KrashExpressionLiteralCallableArgument>, private val inheritClass: KrashValueClass?, private val inheritArgs: List<KrashExpression>?, private val init: (runtime: KrashRuntime, argumentList: List<KrashValue>) -> List<KrashValueClassMember>): KrashValueSimple(), KrashValueClassType {

    companion object {

        private val nativeObjects: HashMap<String, KrashValueClassType> = hashMapOf(

            // File Object
            Pair("File", KrashValueClass("File", null, KrashValueClassModifier.NONE, listOf(), null, null) {_: KrashRuntime, argumentList: List<KrashValue> ->

                // Define Values
                val path = argumentList.let {

                    // Custom Path
                    if(it.isNotEmpty()) it[0].let {

                        // Invalid Type
                        if(it !is KrashValueString) throw KrashRuntimeException("File path must be a string!")

                        // Return Value
                        it.getValue().let {

                            // Absolute Path
                            if(KrashFileSystem.isAbsolutePath((it))) it

                            // Relative Path
                            else "${KrashRuntime.cwd()}/$it"
                        }
                    }

                    // Default Path
                    else KrashRuntime.cwd()
                }
                val file = File(path)

                // Return Members
                listOf(
                    KrashValueClassMember("copy", KrashValueCallable.anon {runtime: KrashRuntime, argumentList: List<KrashValue> ->

                        // Validate Arguments
                        if(argumentList.isEmpty()) throw KrashRuntimeException("Must supply destination!")

                        // Destination Argument
                        val destination = File(argumentList[0].toSimple(runtime).let {

                            // Invalid Type
                            if(it !is KrashValueString) throw KrashRuntimeException("File copy destination must be a string!")

                            // Return Value
                            it.getValue().let {

                                // Absolute Path
                                if(KrashFileSystem.isAbsolutePath((it))) it

                                // Relative Path
                                else "${KrashRuntime.cwd()}/$it"
                            }
                        })

                        // Invalid File
                        if(!file.exists()) throw KrashRuntimeException("Could not find file to copy!")

                        // Copy Directory
                        if(file.isDirectory) file.copyRecursively(destination, true)

                        // Copy File
                        else file.copyTo(destination, true)

                        // Done
                        KrashValueNull()
                    }),
                    KrashValueClassMember("isDirectory", KrashValueBoolean(file.isDirectory)),
                    KrashValueClassMember("files", KrashValueCallable.anon {_: KrashRuntime, _: List<KrashValue> ->
                        KrashValueArray(ArrayList<KrashValue>().apply {
                            file.list().forEach {
                                add(KrashValueString(it))
                                // NOTE: would be more useful to have these as file objects
                            }
                        })
                    }),
                    KrashValueClassMember("path", KrashValueString(path)),
                    KrashValueClassMember("toString", KrashValueCallable.anon { _: KrashRuntime, _: List<KrashValue> ->
                        KrashValueString(path)
                    })
                )
            }),

            // Network Object
            Pair("Network", KrashValueClassStatic("Network", listOf(

                // Server Object
                KrashValueClassMember("createServer", KrashValueClass("Server", null, KrashValueClassModifier.NONE, listOf(), null, null) {runtime: KrashRuntime, argumentList: List<KrashValue> ->

                    // Validate Arguments
                    if(argumentList.isEmpty()) throw KrashRuntimeException("No value provided for port!")
                    if(argumentList.size < 2) throw KrashRuntimeException("No value provided for logic!")

                    // Define Values
                    val port = argumentList[0].toSimple(runtime).let {

                        // Invalid Type
                        if(it !is KrashValueInteger) throw KrashRuntimeException("Server port must be an integer!")

                        // Return Value
                        it.value
                    }
                    val logic: KrashValueCallable = argumentList[1].toSimple(runtime).let {

                        // Invalid Type
                        if(it !is KrashValueCallable) throw KrashRuntimeException("Server logic must be callable!")

                        // Return Value
                        it
                    }

                    // Create Server
                    KrashServer(port) {request: KrashServerRequest, response: (response: String) -> Unit ->

                        // Invoke Logic
                        val responseBody: String = logic.invoke(runtime, listOf(KrashValueString(request.path), KrashValueString(request.method), KrashValueMap(ArrayList<KrashValueMapPair>().apply {
                            request.parameter.forEach {k, v ->
                                add(KrashValueMapPair(k, KrashValueString(v.toString())))
                            }
                        }))).toSimple(runtime).let {

                            // Invalid Type
                            if(it !is KrashValueString) throw KrashRuntimeException("Response body must be a string!")

                            // Return Value
                            it.getValue()
                        }

                        // Invoke Response
                        response(responseBody)
                    }

                    // Return Members
                    listOf()
                }),

                // Request Object
                KrashValueClassMember("request", KrashValueClass("Request", null, KrashValueClassModifier.NONE, listOf(), null, null) { _: KrashRuntime, argumentList: List<KrashValue> ->

                    // Validate Arguments
                    if(argumentList.isEmpty()) throw KrashRuntimeException("Must supply url!")

                    // Define Values
                    val url = argumentList[0].let {

                        // Invalid Type
                        if(it !is KrashValueString) throw KrashRuntimeException("Request url must be a string!")

                        // Return Value
                        it.getValue()
                    }

                    // Return Members
                    listOf(
                        KrashValueClassMember("send", KrashValueCallable.anon {_: KrashRuntime, _: List<KrashValue> ->

                            // Send Request
                            with(URL(url).openConnection() as HttpURLConnection) {

                                // Return Result
                                KrashValueMap(listOf(
                                    KrashValueMapPair("body", KrashValueString(inputStream.bufferedReader().readText())),
                                    KrashValueMapPair("status", KrashValueInteger(responseCode))
                                ))
                            }
                        }),
                        KrashValueClassMember("toString", KrashValueCallable.anon { _: KrashRuntime, _: List<KrashValue> ->
                            KrashValueString(url)
                        })
                    )
                })
            )))
        )

        fun nativeContains(name: String) = nativeObjects.containsKey(name)

        fun nativeGet(name: String): KrashValueClassType {

            // Return Object
            if(nativeObjects.containsKey(name)) return nativeObjects[name]!!

            // Invalid Key
            throw KrashRuntimeException("Could not find '$name' native object!")
        }

        fun nativeReserved() = nativeObjects.keys

    }

    open fun create(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValueObject {

        // Abstract Class
        if(isAbstract()) throw KrashRuntimeException("Cannot instantiate abstract '$name' class!")

        // Heap Inject
        constructorList.forEachIndexed {pos, arg ->
            classRuntime?.heapPut(arg.name, argumentList[pos].let {

                // Argument Value
                if(pos < argumentList.size) it.let {

                    // Cast String
                    if(arg.modifier == KrashExpressionLiteralCallableArgumentModifier.STRING) it.toStringType()

                    // Keep Value
                    it.toSimple(runtime)
                }

                // Default Value
                else arg.defaultValue(runtime)
            })
        }

        // NOTE: this is correctly injecting the values passed into the constructor into the runtime
        //       when we try and invoke a method from an extending class, the (potentially different) references don't lookup the correct runtime
        //       a separate collection needs to exist, containing the values that passed into the extended class with correct names

        // TEMP DEBUG
        println("")
        println("CLASS INIT")
        println(" class  : ${this.name}")
        println(" extends: ${inheritClass?.name}")
        println(" args   : $argumentList")
        println(" heap   : ${classRuntime?.heapData()}")

        // Create Object
        return KrashValueObject(this, ArrayList<KrashValueClassMember>().apply {

            // NOTE: might want to simply init the super class as if it was separate
            //       then defer method calls to super (which has separate heap)

            // Class Info
            add(KrashValueClassMember("class", this@KrashValueClass))

            // Super Members
            addAll(inheritedMembers())

            // Class Members
            addAll(init(runtime, argumentList).apply {

                // Iterate Methods
                this.filterIsInstance<KrashValueClassMemberMethod>().forEach {
                    // NOTE: need to check overriding methods
                    //       come back to this when abstract and open method modifiers have been sorted
                }
            })
        })
    }

    override fun getName() = name

    private fun inheritedMembers(): List<KrashValueClassMember> {

        // Class Runtime
        return classRuntime?.let {

            // Super Class
            inheritClass?.let {classSuper ->
                ArrayList<KrashValueClassMember>().apply {

                    // Super Constructor
                    addAll(classSuper.init.invoke(classRuntime, inheritArgs?.map {

                        // Resolve Argument
                        it.toValue(classRuntime)
                    } ?: listOf()))

                    // Super Inherited
                    addAll(classSuper.inheritedMembers())
                }
            }
        } ?: listOf()
    }

    fun isAbstract() = modifier == KrashValueClassModifier.ABSTRACT

    fun isFinal() = !isAbstract() && modifier != KrashValueClassModifier.OPEN

    override fun toString() = "<class $name>"

}

open class KrashValueClassMember(private val name: String, private val value: KrashValueSimple, private val anon: Boolean = false): KrashValueSimple() {

    companion object {

        fun anon(value: KrashValueSimple) = KrashValueClassMember("", value, true)

    }

    fun getName() = name

    fun getValue() = value

    fun isAnon() = anon

}

class KrashValueClassMemberMethod(name: String, value: KrashValueCallable): KrashValueClassMember(name, value) {

    // NOTE: this is where additional logic to handle the heap specific to the class that the method resides in can be accessed for invoke

}

enum class KrashValueClassModifier {
    ABSTRACT, NONE, OPEN
}

class KrashValueClassStatic(private val name: String, memberList: List<KrashValueClassMember>): KrashValueSimple(), KrashValueClassType {

    init {
        memberList.forEach {
            memberPut(it.getName(), it.getValue())
        }
    }

    override fun getName() = name

    override fun toString() = "<class $name>"

}

interface KrashValueClassType {

    fun getName(): String

}

class KrashValueObject(private val obj: KrashValueClass, memberList: List<KrashValueClassMember>): KrashValueSimple(HashMap<String, KrashValue>().apply {
    memberList.forEach {
        put(it.getName(), it.getValue())
    }
}) {

    fun getClass() = obj

    fun isEqual(value: KrashValueObject): Boolean {

        // Compare Class
        if(obj.getName() != value.obj.getName()) return false

        // Compare Content
        return serialiseExists() && serialiseData() == value.serialiseData()
    }

    private fun serialiseData() = memberGet("serialise").let {

        // Custom String
        if(it is KrashValueString) it.getValue()

        // Cast String
        else it.toString()
    }

    private fun serialiseExists() = memberContains("serialise")

    override fun toString(): String {

        // Invoke Member
        if(memberContains("toString")) memberGet("toString").let {

            // Custom String
            if(it is KrashValueString) it.getValue()

            // Cast String
            else it.toString()
        }

        // Default Value
        return "<object ${obj.getName()}>"
    }

}