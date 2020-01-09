package craicoverflow89.krash.tests

import craicoverflow89.krash.KrashTest
import craicoverflow89.krash.components.objects.KrashValueArray
import craicoverflow89.krash.components.objects.KrashValueInteger
import craicoverflow89.krash.components.objects.KrashValueString
import org.junit.Assert
import org.junit.Test

class KrashArrayTest: KrashTest() {

    @Test
    fun add() = with(invokeLines(listOf("list = [0, 1, 2]", "list.add(3)", "list"))) {
        Assert.assertTrue(this is KrashValueArray)
        (this as KrashValueArray).let {
            Assert.assertEquals("[0, 1, 2, 3]", it.toString())
            Assert.assertEquals(3, (it.getElement(3) as KrashValueInteger).value)
            Assert.assertEquals(4, it.getSize())
        }
    }

    @Test
    fun each() {
        invokeLine("[0, 1, 2].each(echo)")
        Assert.assertEquals(listOf("0", "1", "2"), output.outGet())
    }

    @Test
    fun filter() = with(invokeLine("[0, 1, 2].filter(fun(){return it < 2})")) {
        Assert.assertTrue(this is KrashValueArray)
        (this as KrashValueArray).let {
            Assert.assertEquals("[0, 1]", it.toString())
            Assert.assertEquals(2, it.getSize())
        }
    }

    @Test
    fun index() = with(invokeLine("[0, 1, 2][1]")) {
        Assert.assertTrue(this is KrashValueInteger)
        Assert.assertEquals(1, (this as KrashValueInteger).value)
    }

    @Test
    fun join() {

        // Default
        with(invokeLine("[0, 1, 2].join()")) {
            Assert.assertTrue(this is KrashValueString)
            Assert.assertEquals("012", (this as KrashValueString).getValue())
        }

        // Separator
        with(invokeLine("[0, 1, 2].join(\"|\")")) {
            Assert.assertTrue(this is KrashValueString)
            Assert.assertEquals("0|1|2", (this as KrashValueString).getValue())
        }

        // Prefix and Postfix
        with(invokeLine("[0, 1, 2].join(\"|\", \"<\", \">\")")) {
            Assert.assertTrue(this is KrashValueString)
            Assert.assertEquals("<0|1|2>", (this as KrashValueString).getValue())
        }
    }

    @Test
    fun literal() = with(invokeLine("[0, 1, 2]")) {
        Assert.assertTrue(this is KrashValueArray)
        (this as KrashValueArray).let {
            Assert.assertEquals("[0, 1, 2]", it.toString())
            Assert.assertEquals(3, it.getSize())
        }
    }

    @Test
    fun map() = with(invokeLine("[0, 1, 2].map(fun(){return it * 10})")) {
        Assert.assertTrue(this is KrashValueArray)
        (this as KrashValueArray).let {
            Assert.assertEquals("[0, 10, 20]", it.toString())
            Assert.assertEquals(3, it.getSize())
        }
    }

    @Test
    fun reject() = with(invokeLine("[0, 1, 2].reject(fun(){return it < 2})")) {
        Assert.assertTrue(this is KrashValueArray)
        (this as KrashValueArray).let {
            Assert.assertEquals("[2]", it.toString())
            Assert.assertEquals(1, it.getSize())
        }
    }

    @Test
    fun size() = with(invokeLine("[0, 1, 2].size()")) {
        Assert.assertTrue(this is KrashValueInteger)
        Assert.assertEquals(3, (this as KrashValueInteger).value)
    }

}