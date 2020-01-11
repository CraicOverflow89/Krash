package craicoverflow89.krash.components.tests

import craicoverflow89.krash.components.KrashComponentTest
import craicoverflow89.krash.components.objects.KrashValueInteger
import org.junit.Assert
import org.junit.Test

class KrashValueTest: KrashComponentTest() {

    @Test
    fun apply() {

        // Short Syntax
        invokeLines("value = 7.apply {return it + 1}", "value").let {
            Assert.assertEquals(true, it is KrashValueInteger)
            Assert.assertEquals(7, (it as KrashValueInteger).value)
        }
        invokeLines("value = 8.apply() {return it + 1}", "value").let {
            Assert.assertEquals(true, it is KrashValueInteger)
            Assert.assertEquals(8, (it as KrashValueInteger).value)
        }

        // Full Syntax
        invokeLines("value = 9.apply(fun() = it + 1)", "value").let {
            Assert.assertEquals(true, it is KrashValueInteger)
            Assert.assertEquals(9, (it as KrashValueInteger).value)
        }
        invokeLines("value = 10.apply(fun() {return it + 1})", "value").let {
            Assert.assertEquals(true, it is KrashValueInteger)
            Assert.assertEquals(10, (it as KrashValueInteger).value)
        }
    }

    @Test
    fun let() {

        // Short Syntax
        invokeLines("value = 7.let {return it + 1}", "value").let {
            Assert.assertEquals(true, it is KrashValueInteger)
            Assert.assertEquals(8, (it as KrashValueInteger).value)
        }
        invokeLines("value = 8.let() {return it + 1}", "value").let {
            Assert.assertEquals(true, it is KrashValueInteger)
            Assert.assertEquals(9, (it as KrashValueInteger).value)
        }

        // Full Syntax
        invokeLines("value = 9.let(fun() = it + 1)", "value").let {
            Assert.assertEquals(true, it is KrashValueInteger)
            Assert.assertEquals(10, (it as KrashValueInteger).value)
        }
        invokeLines("value = 10.let(fun() {return it + 1})", "value").let {
            Assert.assertEquals(true, it is KrashValueInteger)
            Assert.assertEquals(11, (it as KrashValueInteger).value)
        }
    }

}