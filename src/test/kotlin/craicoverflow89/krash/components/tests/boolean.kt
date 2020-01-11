package craicoverflow89.krash.components.tests

import craicoverflow89.krash.components.KrashComponentTest
import craicoverflow89.krash.components.objects.KrashValueBoolean
import craicoverflow89.krash.components.objects.KrashValueString
import org.junit.Assert
import org.junit.Test

class KrashBooleanTest: KrashComponentTest() {

    @Test
    fun castString() {

        // True
        with(invokeLine("true.toString()")) {
            Assert.assertTrue(this is KrashValueString)
            (this as KrashValueString).let {
                Assert.assertEquals("true", it.getValue())
            }
        }

        // False
        with(invokeLine("false.toString()")) {
            Assert.assertTrue(this is KrashValueString)
            (this as KrashValueString).let {
                Assert.assertEquals("false", it.getValue())
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