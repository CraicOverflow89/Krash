package craicoverflow89.krash.components

import craicoverflow89.krash.components.objects.KrashValue
import craicoverflow89.krash.components.objects.KrashValueCallable
import craicoverflow89.krash.components.objects.KrashValueNull
import craicoverflow89.krash.components.objects.KrashValueString

open class KrashMethod(name: String, logic: (runtime: KrashRuntime, argumentList: List<KrashValue>) -> KrashValue): KrashValueCallable(name, logic) {

    companion object {

        private val nativeMethods = mapOf(
            Pair("echo", KrashMethodEcho()),
            Pair("exit", KrashMethodExit()),
            Pair("include", KrashMethodInclude()),
            Pair("read", KrashMethodRead())
        )

        fun nativeContains(name: String) = nativeMethods.containsKey(name)

        fun nativeGet(name: String) = nativeMethods[name] ?: throw KrashRuntimeException("Could not find '$name' native method!")

        fun nativeReserved() = nativeMethods.keys

    }

}

class KrashMethodEcho: KrashMethod("echo", fun(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue {

    // Print Values
    argumentList.forEach {
        KrashRuntime.println(it.toSimple(runtime))
    }

    // Done
    return KrashValueNull()
})

class KrashMethodExit: KrashMethod("exit", fun(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue {

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

class KrashMethodInclude: KrashMethod("include", fun(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue {

    // Validate Arguments
    if(argumentList.isEmpty()) throw KrashRuntimeException("No value provided for path!")

    // Resolve Path
    argumentList[0].toSimple(runtime).let {

        // Include Script
        if(it is KrashValueString) runtime.includeScript(it.getValue())

        // Invalid Type
        else throw KrashRuntimeException("Path must be a string!")

    }

    // Done
    return KrashValueNull()
})

class KrashMethodRead: KrashMethod("read", fun(_: KrashRuntime, _: List<KrashValue>) = KrashValueString(KrashRuntime.read()))