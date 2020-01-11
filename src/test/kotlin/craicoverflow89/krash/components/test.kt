package craicoverflow89.krash.components

import craicoverflow89.krash.components.objects.KrashValue
import craicoverflow89.krash.components.objects.KrashValueNull

open class KrashComponentTest {

    protected val channel = KrashTestChannel()
    private val runtime = KrashRuntime(System.getProperty("user.dir")).apply {
        KrashRuntime.channelSet(channel)
    }

    fun invokeLine(value: String, runtimePersist: Boolean = true): KrashValue {
        channel.clear()
        return parseLine(value).invoke(runtime).apply {
            if(!runtimePersist) runtime.empty()
        }
    }

    fun invokeLines(vararg value: String): KrashValue {
        channel.clear()
        var result: KrashValue = KrashValueNull()
        value.forEach {
            result = invokeLine(it, true)
        }
        runtime.empty()
        return result
    }

    fun parseLine(value: String) = KrashInterpreter.parseCommand(value)

    fun parseLines(value: List<String>) = parseLine(value.joinToString("\n"))

}

class KrashTestChannel: KrashChannel() {

    private val errList = ArrayList<String>()
    private val outList = ArrayList<String>()

    override fun err(text: String) {
        errList.add(text)
    }

    fun clear() {
        errList.clear()
        outList.clear()
    }

    fun errGet(): List<String> = errList

    override fun out(text: String) {
        outList.add(text)
    }

    fun outGet(): List<String> = outList

}