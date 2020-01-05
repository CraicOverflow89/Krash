package craicoverflow89.krash.components.objects

import craicoverflow89.krash.components.KrashRuntime
import java.io.File

class KrashValueClass(val name: String, private val init: (runtime: KrashRuntime, argumentList: List<KrashValue>) -> HashMap<String, KrashValue>): KrashValueSimple() {

    companion object {

        private val nativeObjects: HashMap<String, KrashValueClass> = hashMapOf(

            // File Object
            Pair("file", KrashValueClass("file") {runtime: KrashRuntime, argumentList: List<KrashValue> ->

                // Define Values
                val path = argumentList.let {

                    // Custom Path
                    if(it.isNotEmpty()) (it[0] as KrashValueString).value

                    // Default Path
                    else runtime.cwd()
                }
                val file = File(path)

                // Return Members
                hashMapOf(
                    Pair("isDirectory", KrashValueBoolean(file.isDirectory)),
                    Pair("files", KrashValueArray(file.list().map {
                        KrashValueString(it)
                    })),
                    Pair("path", KrashValueString(path)),
                    Pair("toString", KrashValueString(path))
                )
            }),

            // Pair Object
            Pair("pair", KrashValueClass("pair") {_: KrashRuntime, argumentList: List<KrashValue> ->

                // Validate Arguments
                if(argumentList.size != 2) throw RuntimeException("Must supply two arguments for pair!")

                // Define Values
                val first = argumentList[0]
                val second = argumentList[1]

                // Return Members
                hashMapOf(
                    Pair("first", first),
                    Pair("second", second),
                    Pair("toString", KrashValueString("<$first, $second>"))
                )
            })
        )

        fun nativeContains(name: String) = nativeObjects.containsKey(name)

        fun nativeGet(name: String): KrashValueClass {

            // Return Object
            if(nativeObjects.containsKey(name)) return nativeObjects[name]!!

            // Invalid Key
            throw RuntimeException("Could not find '$name' native object!")
        }

        fun nativeReserved() = nativeObjects.keys

    }

    fun create(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValueObject {

        // NOTE: validate argumentList against expect constructor args

        // TEMP
        return KrashValueObject(this, init(runtime, argumentList))
        // NOTE: come back to this
    }

    override fun toString() = "<class ${name}>"

}