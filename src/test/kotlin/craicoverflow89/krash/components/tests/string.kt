package craicoverflow89.krash.components.tests

import craicoverflow89.krash.components.KrashComponentTest
import craicoverflow89.krash.components.objects.KrashValueArray
import craicoverflow89.krash.components.objects.KrashValueBoolean
import craicoverflow89.krash.components.objects.KrashValueInteger
import craicoverflow89.krash.components.objects.KrashValueString
import org.junit.Assert
import org.junit.Test

class KrashStringTest: KrashComponentTest() {

    @Test
    fun castString() = with(invokeLine("\"string literal\"")) {
        Assert.assertTrue(this is KrashValueString)
        (this as KrashValueString).let {
            Assert.assertEquals("string literal", it.getValue())
        }
    }

    @Test
    fun comparison() {

        // Equality True
        with(invokeLine("\"text\" == \"text\"")) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(true, (this as KrashValueBoolean).isTrue())
        }

        // Equality False
        with(invokeLine("\"text\" == \"chars\"")) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(false, (this as KrashValueBoolean).isTrue())
        }

        // Inequality True
        with(invokeLine("\"text\" != \"chars\"")) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(true, (this as KrashValueBoolean).isTrue())
        }

        // Inequality False
        with(invokeLine("\"text\" != \"text\"")) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(false, (this as KrashValueBoolean).isTrue())
        }
    }

    @Test
    fun endsWith() {

        // True
        with(invokeLine("\"hello\".endsWith(\"o\")")) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(true, (this as KrashValueBoolean).isTrue())
        }

        // False
        with(invokeLine("\"hello\".endsWith(\"O\")")) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(false, (this as KrashValueBoolean).isTrue())
        }
    }

    @Test
    fun index() = with(invokeLine("\"index\"[4]")) {
        Assert.assertTrue(this is KrashValueString)
        Assert.assertEquals("x", (this as KrashValueString).getValue())
    }

    @Test
    fun literal() {

        // Simple Characters
        with(invokeLine("\"string literal\"")) {
            Assert.assertTrue(this is KrashValueString)
            Assert.assertEquals("string literal", (this as KrashValueString).getValue())
        }

        // Escape Characters
        with(invokeLine("\"string\nliteral with \\\"quotes\\\"\"")) {
            Assert.assertTrue(this is KrashValueString)
            Assert.assertEquals("string\nliteral with \"quotes\"", (this as KrashValueString).getValue())
        }

        // Custom Class Reference
        /*with(invokeLines("\$Test\"")) {
            Assert.assertTrue(this is KrashValueString)
            Assert.assertEquals("<class Test>", (this as KrashValueString).getValue())
        }*/
        // NOTE: need to create a Test class to check this

        // Custom Value Reference
        with(invokeLines("name = \"James\"", "\"\$name\"")) {
            Assert.assertTrue(this is KrashValueString)
            Assert.assertEquals("James", (this as KrashValueString).getValue())
        }

        // Global Reference
        with(invokeLines("\"\$HOME\"")) {
            Assert.assertTrue(this is KrashValueString)
            Assert.assertEquals(System.getProperty("user.home").replace("\\", "/"), (this as KrashValueString).getValue())
        }

        // Native Class Reference
        with(invokeLines("\"\$File\"")) {
            Assert.assertTrue(this is KrashValueString)
            Assert.assertEquals("<class File>", (this as KrashValueString).getValue())
        }
    }

    @Test
    fun size() = with(invokeLine("\"size\".size")) {
        Assert.assertTrue(this is KrashValueInteger)
        Assert.assertEquals(4, (this as KrashValueInteger).value)
    }

    @Test
    fun startsWith() {

        // True
        with(invokeLine("\"hello\".startsWith(\"h\")")) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(true, (this as KrashValueBoolean).isTrue())
        }

        // False
        with(invokeLine("\"hello\".startsWith(\"H\")")) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(false, (this as KrashValueBoolean).isTrue())
        }
    }

    @Test
    fun toList() {

        // Default
        with(invokeLine("\"hello\".toList()")) {
            Assert.assertTrue(this is KrashValueArray)
            (this as KrashValueArray).let {
                Assert.assertEquals("[h, e, l, l, o]", it.toString())
                Assert.assertEquals("l", (it.getElement(2) as KrashValueString).getValue())
            }
        }

        // Delimiter
        with(invokeLine("\"hello world\".toList(\" \")")) {
            Assert.assertTrue(this is KrashValueArray)
            (this as KrashValueArray).let {
                Assert.assertEquals("[hello, world]", it.toString())
                Assert.assertEquals("world", (it.getElement(1) as KrashValueString).getValue())
            }
        }
    }

}