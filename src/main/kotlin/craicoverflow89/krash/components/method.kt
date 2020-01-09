package craicoverflow89.krash.components

import craicoverflow89.krash.components.objects.KrashValue
import craicoverflow89.krash.components.objects.KrashValueCallable
import craicoverflow89.krash.components.objects.KrashValueNull
import craicoverflow89.krash.components.objects.KrashValueString

open class KrashMethod(logic: (runtime: KrashRuntime, argumentList: List<KrashValue>) -> KrashValue): KrashValueCallable(logic) {

    companion object {

        private val nativeMethods = mapOf(
            Pair("echo", KrashMethodEcho()),
            Pair("exit", KrashMethodExit()),
            Pair("read", KrashMethodRead())
        )

        fun nativeContains(name: String) = nativeMethods.containsKey(name)

        fun nativeGet(name: String) = nativeMethods[name] ?: throw KrashRuntimeException("Could not find '$name' native method!")

        fun nativeReserved() = nativeMethods.keys

    }

}

class KrashMethodEcho: KrashMethod(fun(runtime: KrashRuntime, argumentList: List<KrashValue>): KrashValue {

    // Print Values
    argumentList.forEach {
        KrashRuntime.println(it.toSimple(runtime))
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

class KrashMethodRead: KrashMethod(fun(_: KrashRuntime, _: List<KrashValue>) = KrashValueString(KrashRuntime.read()))