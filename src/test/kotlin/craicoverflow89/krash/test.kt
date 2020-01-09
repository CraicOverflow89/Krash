package craicoverflow89.krash

import craicoverflow89.krash.components.KrashInterpreter
import craicoverflow89.krash.components.KrashOutput
import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.components.objects.KrashValue
import craicoverflow89.krash.components.objects.KrashValueNull

open class KrashTest {

    protected val output = KrashTestOutput()
    private val runtime = KrashRuntime().apply {
        KrashRuntime.outputSet(output)
    }

    fun invokeLine(value: String): KrashValue {
        output.clear()
        return parseLine(value).invoke(runtime)
    }

    fun invokeLines(value: List<String>): KrashValue {
        output.clear()
        var result: KrashValue = KrashValueNull()
        value.forEach {
            result = invokeLine(it)
        }
        return result
    }

    fun parseLine(value: String) = KrashInterpreter.parseCommand(value)

    fun parseLines(value: List<String>) = parseLine(value.joinToString("\n"))

}

class KrashTestOutput: KrashOutput() {

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