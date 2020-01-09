package craicoverflow89.krash.tests

import craicoverflow89.krash.KrashTest
import craicoverflow89.krash.components.KrashCommandComment
import org.junit.Assert
import org.junit.Test

class KrashCommentTest: KrashTest() {

    @Test
    fun multiple() = with(parseLine("/* comment\nmultiple */")) {
        Assert.assertTrue(this is KrashCommandComment)
        Assert.assertEquals("comment\nmultiple", (this as KrashCommandComment).getValue())
    }

    @Test
    fun nested() = with(parseLine("/* comment\n// nested\nmultiple */")) {
        Assert.assertTrue(this is KrashCommandComment)
        Assert.assertEquals("comment\n// nested\nmultiple", (this as KrashCommandComment).getValue())
    }

    @Test
    fun single() = with(parseLine("// comment")) {
        Assert.assertTrue(this is KrashCommandComment)
        Assert.assertEquals("comment", (this as KrashCommandComment).getValue())
    }

}