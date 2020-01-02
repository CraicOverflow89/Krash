package craicoverflow89.krash.components

interface KrashValue

class KrashValueArray(val valueList: ArrayList<KrashValue>): KrashValue {

    override fun toString() = valueList.map {
        it.toString()
    }.joinToString(", ", "[", "]")

}

class KrashValueBoolean(val value: Boolean): KrashValue {

    override fun toString() = if(value) "true" else "false"

}

class KrashValueInteger(val value: Integer): KrashValue {

    override fun toString() = value.toString()

}

class KrashValueNull: KrashValue {

    override fun toString() = "null"

}

class KrashValueString(val value: String): KrashValue {

    override fun toString() = value

}

class KrashValueReference(val ref: KrashReference, val byRef: Boolean): KrashValue {

    override fun toString() = ref.value

}