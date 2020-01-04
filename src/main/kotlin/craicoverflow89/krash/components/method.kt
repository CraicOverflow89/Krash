package craicoverflow89.krash.components

open class KrashMethod(logic: (runtime: KrashRuntime, argumentList: List<KrashValue>) -> KrashValue): KrashValueCallable(logic) {

    companion object {

        private val nativeMethods = mapOf(
            Pair("echo", KrashMethodEcho()),
            Pair("exit", KrashMethodExit()),
            Pair("file", KrashMethodFile())
        )

        fun nativeContains(name: String) = nativeMethods.containsKey(name)

        fun nativeGet(name: String) = nativeMethods[name] ?: throw RuntimeException("Could not find '$name' native method!")

        fun nativeReserved() = nativeMethods.keys

    }

}

class KrashMethodEcho: KrashMethod(fun(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue {

    // Print Values
    argumentList.forEach {
        println(it.toSimple(runtime))
    }

    // Done
    return KrashValueNull()
})

class KrashMethodExit: KrashMethod(fun(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue {

    // Runtime Exit
    runtime.exit(argumentList.let {

        // Custom Code
        if(argumentList.isNotEmpty()) Integer.parseInt(argumentList[0].toSimple(runtime).toString())

        // Default Code
        else 0
    })

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