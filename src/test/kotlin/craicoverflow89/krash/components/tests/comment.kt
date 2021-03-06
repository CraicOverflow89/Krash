package craicoverflow89.krash.components.tests

import craicoverflow89.krash.components.KrashCommandComment
import craicoverflow89.krash.components.KrashComponentTest
import craicoverflow89.krash.components.objects.KrashValueCallable
import org.junit.Assert
import org.junit.Test

class KrashCommentTest: KrashComponentTest() {

    @Test
    fun multiple() = with(parseLine("/* comment\nmultiple */")) {
        Assert.assertTrue(this is KrashCommandComment)
        Assert.assertEquals("comment\nmultiple", (this as KrashCommandComment).getValue())
    }

    @Test
    fun nested() {

        // Within Comment
        with(parseLine("/* comment\n// nested\nmultiple */")) {
            Assert.assertTrue(this is KrashCommandComment)
            Assert.assertEquals("comment\n// nested\nmultiple", (this as KrashCommandComment).getValue())
        }

        // Within Function
        with(invokeLine("fun(){\n// comment\n}")) {
            Assert.assertTrue(this is KrashValueCallable)
        }
    }

    @Test
    fun single() = with(parseLine("// comment")) {
        Assert.assertTrue(this is KrashCommandComment)
        Assert.assertEquals("comment", (this as KrashCommandComment).getValue())
    }

}