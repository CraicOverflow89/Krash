package craicoverflow89.krash.tests

import craicoverflow89.krash.KrashTest
import craicoverflow89.krash.components.objects.KrashValueBoolean
import craicoverflow89.krash.components.objects.KrashValueString
import org.junit.Assert
import org.junit.Test

class KrashBooleanTest: KrashTest() {

    @Test
    fun castString() {

        // True
        with(invokeLine("true")) {
            Assert.assertTrue(this is KrashValueString)
            (this as KrashValueString).let {
                Assert.assertEquals("true", it.getValue())
            }
        }

        // False
        with(invokeLine("true")) {
            Assert.assertTrue(this is KrashValueString)
            (this as KrashValueString).let {
                Assert.assertEquals("true", it.getValue())
            }
        }
    }

    @Test
    fun literal() {

        // True
        with(invokeLines("bool = true", "bool")) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(true, (this as KrashValueBoolean).isTrue())
        }

        // False
        with(invokeLines("bool = false", "bool")) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(false, (this as KrashValueBoolean).isTrue())
        }
    }

    @Test
    fun negation() = with(invokeLines("bool = true", "!bool")) {
        Assert.assertTrue(this is KrashValueBoolean)
        Assert.assertEquals(false, (this as KrashValueBoolean).isTrue())
    }

}