package craicoverflow89.krash.components.tests

import craicoverflow89.krash.components.KrashComponentTest
import craicoverflow89.krash.components.objects.KrashValueInteger
import craicoverflow89.krash.components.objects.KrashValueMap
import craicoverflow89.krash.components.objects.KrashValueString
import org.junit.Assert
import org.junit.Test

class KrashNetworkTest: KrashComponentTest() {

    @Test
    fun castString() = "https://www.google.co.uk/".let {path ->
        with(invokeLine("Network.request(\"$path\").toString()")) {
            Assert.assertTrue(this is KrashValueString)
            (this as KrashValueString).let {
                Assert.assertEquals(path, it.getValue())
            }
        }
    }

    @Test
    fun get() = with(invokeLine("Network.request(\"https://www.google.co.uk/\").send()")) {
        Assert.assertTrue(this is KrashValueMap)
        (this as KrashValueMap).getData().let {
            Assert.assertTrue(it.containsKey("body"))
            Assert.assertEquals(200, (it["status"] as KrashValueInteger).value)
        }
    }

}