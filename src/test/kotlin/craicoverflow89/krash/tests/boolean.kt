package craicoverflow89.krash.tests

import craicoverflow89.krash.KrashTest
import craicoverflow89.krash.components.objects.KrashValueBoolean
import org.junit.Assert
import org.junit.Test

class KrashBooleanTest: KrashTest() {

    @Test
    fun literal() {

        // True
        with(invokeLines(listOf("bool = true", "bool"))) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(true, (this as KrashValueBoolean).isTrue())
        }

        // False
        with(invokeLines(listOf("bool = false", "bool"))) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(false, (this as KrashValueBoolean).isTrue())
        }
    }

    @Test
    fun negation() = with(invokeLines(listOf("bool = true", "!bool"))) {
        Assert.assertTrue(this is KrashValueBoolean)
        Assert.assertEquals(false, (this as KrashValueBoolean).isTrue())
    }

}