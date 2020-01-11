package craicoverflow89.krash.components.objects

import craicoverflow89.krash.components.KrashRuntimeException
import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.expressions.KrashExpression
import craicoverflow89.krash.system.KrashFileSystem
import craicoverflow89.krash.system.KrashServer
import craicoverflow89.krash.system.KrashServerRequest
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

open class KrashValueClass(val name: String, private val classRuntime: KrashRuntime?, private val modifier: KrashValueClassModifier, private val inheritClass: KrashValueClass?, private val inheritArgs: List<KrashExpression>?, private val init: (runtime: KrashRuntime, argumentList: List<KrashValue>) -> HashMap<String, KrashValue>): KrashValueSimple() {

    companion object {

        private val nativeObjects: HashMap<String, KrashValue> = hashMapOf(

            // File Object
            Pair("File", KrashValueClass("File", null, KrashValueClassModifier.NONE, null, null) {_: KrashRuntime, argumentList: List<KrashValue> ->

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
                hashMapOf(
                    Pair("copy", KrashValueCallable {runtime: KrashRuntime, argumentList: List<KrashValue> ->

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
                    Pair("isDirectory", KrashValueBoolean(file.isDirectory)),
                    Pair("files", KrashValueCallable {_: KrashRuntime, _: List<KrashValue> ->
                        KrashValueArray(ArrayList<KrashValue>().apply {
                            file.list().forEach {
                                add(KrashValueString(it))
                                // NOTE: would be more useful to have these as file objects
                            }
                        })
                    }),
                    Pair("path", KrashValueString(path)),
                    Pair("toString", KrashValueCallable { _: KrashRuntime, _: List<KrashValue> ->
                        KrashValueString(path)
                    })
                )
            }),

            // Network Object
            Pair("Network", KrashValueClassStatic("Network", hashMapOf(

                // Server Object
                Pair("createServer", KrashValueClass("Server", null, KrashValueClassModifier.NONE, null, null) {runtime: KrashRuntime, argumentList: List<KrashValue> ->

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
                    hashMapOf()
                }),

                // Request Object
                Pair("request", KrashValueClass("Request", null, KrashValueClassModifier.NONE, null, null) { _: KrashRuntime, argumentList: List<KrashValue> ->

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
                    hashMapOf(
                        Pair("send", KrashValueCallable {_: KrashRuntime, _: List<KrashValue> ->

                            // Send Request
                            with(URL(url).openConnection() as HttpURLConnection) {

                                // Return Result
                                KrashValueMap(listOf(
                                    KrashValueMapPair("body", KrashValueString(inputStream.bufferedReader().readText())),
                                    KrashValueMapPair("status", KrashValueInteger(responseCode))
                                ))
                            }
                        }),
                        Pair("toString", KrashValueCallable { _: KrashRuntime, _: List<KrashValue> ->
                            KrashValueString(url)
                        })
                    )
                })
            )))
        )

        fun nativeContains(name: String) = nativeObjects.containsKey(name)

        fun nativeGet(name: String): KrashValue {

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

        // Create Object
        return KrashValueObject(this, init(runtime, argumentList).apply {

            // Class Info
            put("class", this@KrashValueClass)

            // Inherit Methods
            inheritedMethods(runtime).forEach {
                put(it.key, it.value)
            }
        })
    }

    private fun inheritedMethods(runtime: KrashRuntime) = HashMap<String, KrashValue>().apply {

        // Class Runtime
        classRuntime?.let {

            // Add Methods
            inheritClass?.init?.invoke(runtime, inheritArgs?.map {

                // Resolve Argument
                it.toValue(classRuntime)
            } ?: listOf())?.forEach {
                put(it.key, it.value)
            }
        }
    }

    fun isAbstract() = modifier == KrashValueClassModifier.ABSTRACT

    fun isFinal() = !isAbstract() && modifier != KrashValueClassModifier.OPEN

    override fun toString() = "<class $name>"

}

enum class KrashValueClassModifier {
    ABSTRACT, NONE, OPEN
}

class KrashValueClassStatic(private val name: String, memberData: HashMap<String, KrashValue>): KrashValueSimple() {

    init {
        memberData.forEach {k, v ->
            memberPut(k, v)
        }
    }

    override fun toString() = "<class $name>"

}