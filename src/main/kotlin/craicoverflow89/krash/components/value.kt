package craicoverflow89.krash.components

interface KrashValue

class KrashValueBoolean(private val value: Boolean): KrashValue

class KrashValueInteger(private val value: Integer): KrashValue

class KrashValueNull: KrashValue

class KrashValueString(private val value: String): KrashValue

class KrashValueReference(private val ref: KrashReference): KrashValue