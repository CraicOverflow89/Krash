package craicoverflow89.krash.components

interface KrashMethod

class KrashMethodNative(private val type: KrashMethodNativeType): KrashMethod

enum class KrashMethodNativeType {
    ECHO
}

class KrashMethodReference(private val ref: KrashReference): KrashMethod