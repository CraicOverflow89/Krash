package craicoverflow89.krash.components.objects

import craicoverflow89.krash.components.KrashRuntimeException
import craicoverflow89.krash.components.KrashRuntime
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class KrashValueClass(val name: String, private val init: (runtime: KrashRuntime, argumentList: List<KrashValue>) -> HashMap<String, KrashValue>): KrashValueSimple() {

    companion object {

        private val nativeObjects: HashMap<String, KrashValueClass> = hashMapOf(

            // File Object
            Pair("file", KrashValueClass("file") {_: KrashRuntime, argumentList: List<KrashValue> ->

                // Define Values
                val path = argumentList.let {

                    // Custom Path
                    if(it.isNotEmpty()) it[0].let {

                        // Invalid Type
                        if(it !is KrashValueString) throw KrashRuntimeException("File path must be a string!")

                        // Return Value
                        it.getValue()
                    }

                    // Default Path
                    else KrashRuntime.cwd()
                }
                val file = File(path)

                // Return Members
                hashMapOf(
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
            Pair("network", KrashValueClass("network") { _: KrashRuntime, argumentList: List<KrashValue> ->

                // Validate Arguments
                if(argumentList.isEmpty()) throw KrashRuntimeException("Must supply url!")

                // Define Values
                val url = argumentList[0].let {

                    // Invalid Type
                    if(it !is KrashValueString) throw KrashRuntimeException("Network url must be a string!")

                    // Return Value
                    it.getValue()
                }
                /*val method = if(argumentList.size > 1) argumentList[1].let {

                    // Invalid Type
                    // NOTE: throw if not string

                    // Invalid Valid
                    // NOTE: throw if not GET or POST
                } else "GET"*/

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
            }),

            // Pair Object
            Pair("pair", KrashValueClass("pair") {_: KrashRuntime, argumentList: List<KrashValue> ->

                // Validate Arguments
                if(argumentList.size != 2) throw KrashRuntimeException("Must supply two arguments for pair!")

                // Define Values
                val first = argumentList[0]
                val second = argumentList[1]

                // Return Members
                hashMapOf(
                    Pair("first", first),
                    Pair("second", second),
                    Pair("toString", KrashValueCallable { _: KrashRuntime, _: List<KrashValue> ->
                        KrashValueString("<$first, $second>")
                    })
                )
            })
        )

        fun nativeContains(name: String) = nativeObjects.containsKey(name)

        fun nativeGet(name: String): KrashValueClass {

            // Return Object
            if(nativeObjects.containsKey(name)) return nativeObjects[name]!!

            // Invalid Key
            throw KrashRuntimeException("Could not find '$name' native object!")
        }

        fun nativeReserved() = nativeObjects.keys

    }

    fun create(runtime: KrashRuntime, argumentList: List<KrashValue>) = KrashValueObject(this, init(runtime, argumentList))

    override fun toString() = "<class ${name}>"

}