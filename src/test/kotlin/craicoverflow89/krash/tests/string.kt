package craicoverflow89.krash.tests

import craicoverflow89.krash.KrashTest
import craicoverflow89.krash.components.objects.KrashValueArray
import craicoverflow89.krash.components.objects.KrashValueBoolean
import craicoverflow89.krash.components.objects.KrashValueInteger
import craicoverflow89.krash.components.objects.KrashValueString
import org.junit.Assert
import org.junit.Test

class KrashStringTest: KrashTest() {

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

        // Reference Syntax
        with(invokeLines(listOf("name = \"James\"", "\"\$name\""))) {
            Assert.assertTrue(this is KrashValueString)
            Assert.assertEquals("James", (this as KrashValueString).getValue())
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