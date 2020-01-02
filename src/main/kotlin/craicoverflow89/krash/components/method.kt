package craicoverflow89.krash.components

open class KrashMethod(logic: (runtime: KrashRuntime, argumentList: List<KrashValue>) -> KrashValue): KrashValueCallable(logic) {

    companion object {

        val nativeMethods = mapOf(
            Pair("echo", KrashMethodEcho()),
            Pair("file", KrashMethodFile())
        )

        fun nativeContains(name: String) = nativeMethods.containsKey(name)

        fun nativeGet(name: String) = nativeMethods[name] ?: throw RuntimeException("Could not find '$name' native method!")

    }

    override fun toString() = "<native method>"

}

class KrashMethodEcho: KrashMethod(fun(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue {

    // Resolution Logic
    fun resolve(value: KrashValue): String = when(value) {

        // Resolve Array
        is KrashValueArray -> value.valueList.joinToString(", ", "[", "]") {
            resolve(it)
        }

        // Resolve Index
        is KrashValueIndex -> resolve(value.resolve(runtime))

        // Resolve Map
        is KrashValueMap -> value.valueList.joinToString(", ", "[", "]") {
            "${it.key}: ${resolve(it.value)}"
        }

        // Resolve Reference
        is KrashValueReference -> {
            //if(!runtime.heapContains(it.ref))
            // NOTE: come back to validation
            resolve(runtime.heapGet(value.ref))
        }


        // Resolve Primitive
        else -> value.toString()
    }

    // Print Values
    argumentList.forEach {
        println(it.toSimple(runtime))
    }

    // Done
    return KrashValueNull()
})

class KrashMethodFile: KrashMethod(fun(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue {

    // Current Directory
    return KrashValueFile(if(argumentList.isEmpty()) runtime.cwd()

    // Path Supplied
    else argumentList[0].toSimple(runtime).toString().let {

        // NOTE: throw exception if it contains '\'
        //       handle instances of '..' for parent directory

        // Absolute Path
        if(it.startsWith("/") || it.matches("^[A-Za-z]:.*\$".toRegex())) it

        // Relative Path
        else runtime.cwdJoin(if(it.startsWith(".")) it.split("/").let {
            it.subList(1, it.size)
        }.joinToString("/") else it)
    })
})