package craicoverflow89.krash.tests

import craicoverflow89.krash.KrashTest
import craicoverflow89.krash.components.objects.KrashValueInteger
import craicoverflow89.krash.components.objects.KrashValueMap
import craicoverflow89.krash.components.objects.KrashValueString
import org.junit.Assert
import org.junit.Test

class KrashNetworkTest: KrashTest() {

    @Test
    fun castString() = "https://www.google.co.uk/".let {path ->
        with(invokeLine("network(\"$path\").toString()")) {
            Assert.assertTrue(this is KrashValueString)
            (this as KrashValueString).let {
                Assert.assertEquals(path, it.getValue())
            }
        }
    }

    @Test
    fun get() = with(invokeLine("network(\"https://www.google.co.uk/\").send()")) {
        Assert.assertTrue(this is KrashValueMap)
        (this as KrashValueMap).getData().let {
            Assert.assertTrue(it.containsKey("body"))
            Assert.assertEquals(200, (it["status"] as KrashValueInteger).value)
        }
    }

}