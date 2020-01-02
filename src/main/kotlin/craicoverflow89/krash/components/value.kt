package craicoverflow89.krash.components

interface KrashValue

class KrashValueArray(val valueList: List<KrashValue>): KrashValue {

    override fun toString() = valueList.joinToString(", ", "[", "]") {
        it.toString()
    }

}

class KrashValueBoolean(val value: Boolean): KrashValue {

    override fun toString() = if(value) "true" else "false"

}

class KrashValueInteger(val value: Integer): KrashValue {

    override fun toString() = value.toString()

}

class KrashValueMap(val valueList: List<KrashValueMapPair>): KrashValue {

    override fun toString() = valueList.joinToString(", ", "{", "}") {
        it.toString()
    }

}

class KrashValueMapPair(val key: String, val value: KrashValue) {

    override fun toString() = "$key: $value"

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