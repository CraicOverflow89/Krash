package craicoverflow89.krash.components.tests

import craicoverflow89.krash.components.KrashComponentTest
import craicoverflow89.krash.components.objects.KrashValueArray
import craicoverflow89.krash.components.objects.KrashValueBoolean
import craicoverflow89.krash.components.objects.KrashValueEnum
import craicoverflow89.krash.components.objects.KrashValueInteger
import craicoverflow89.krash.components.objects.KrashValueNull
import craicoverflow89.krash.components.objects.KrashValueString
import org.junit.Assert
import org.junit.Test

class KrashEnumTest: KrashComponentTest() {

    @Test
    fun castString() = with(invokeLines("enum Direction {EAST, NORTH, SOUTH, WEST}", "Direction.toString()")) {
        Assert.assertTrue(this is KrashValueString)
        (this as KrashValueString).let {
            Assert.assertEquals("<enum Direction>", it.getValue())
        }
    }

    @Test
    fun comparison() {

        // Equality True
        with(invokeLines("enum Direction {EAST, NORTH, SOUTH, WEST}", "Direction.NORTH == Direction.NORTH")) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(true, (this as KrashValueBoolean).isTrue())
        }

        // Equality False
        with(invokeLines("enum Direction {EAST, NORTH, SOUTH, WEST}", "Direction.NORTH == Direction.SOUTH")) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(false, (this as KrashValueBoolean).isTrue())
        }

        // Inequality True
        with(invokeLines("enum Direction {EAST, NORTH, SOUTH, WEST}", "Direction.NORTH != Direction.SOUTH")) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(true, (this as KrashValueBoolean).isTrue())
        }

        // Inequality False
        with(invokeLines("enum Direction {EAST, NORTH, SOUTH, WEST}", "Direction.NORTH != Direction.NORTH")) {
            Assert.assertTrue(this is KrashValueBoolean)
            Assert.assertEquals(false, (this as KrashValueBoolean).isTrue())
        }
    }

    @Test
    fun create() = with(invokeLines("enum Direction {EAST, NORTH, SOUTH, WEST}", "Direction")) {
        Assert.assertTrue(this is KrashValueEnum)
        (this as KrashValueEnum).let {
            Assert.assertEquals(listOf("EAST", "NORTH", "SOUTH", "WEST"), it.getValues())
        }
    }

    @Test
    fun valueOf() {

        // Valid Name
        with(invokeLines("enum Direction {EAST, NORTH, SOUTH, WEST}", "Direction.valueOf(\"NORTH\")")) {
            Assert.assertTrue(this is KrashValueInteger)
            (this as KrashValueInteger).let {
                Assert.assertEquals(1, it.value)
            }
        }

        // Invalid Name
        with(invokeLines("enum Direction {EAST, NORTH, SOUTH, WEST}", "Direction.valueOf(\"INVALID\")")) {
            Assert.assertTrue(this is KrashValueNull)
        }
    }

    @Test
    fun values() = with(invokeLines("enum Direction {EAST, NORTH, SOUTH, WEST}", "Direction.values")) {
        Assert.assertTrue(this is KrashValueArray)
        (this as KrashValueArray).let {
            Assert.assertEquals(4, it.getSize())
            it.getValue().let {
                val nameList = listOf("EAST", "NORTH", "SOUTH", "WEST")
                it.forEachIndexed {id, name ->
                    Assert.assertEquals(true, name is KrashValueString)
                    Assert.assertEquals(nameList[id], (name as KrashValueString).getValue())
                }
            }
        }
    }

}