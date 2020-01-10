package craicoverflow89.krash.components.tests

import craicoverflow89.krash.components.KrashComponentTest
import craicoverflow89.krash.components.objects.KrashValueArray
import craicoverflow89.krash.components.objects.KrashValueBoolean
import craicoverflow89.krash.components.objects.KrashValueObject
import craicoverflow89.krash.components.objects.KrashValueString
import org.junit.Assert
import org.junit.Test
import java.io.File

class KrashFileTest: KrashComponentTest() {

    // NOTE: need to be able to create a temporary directory (in user home?)
    //       can create files and directories in here to copy, move, rename, etc...
    //       delete the temporary directory once tests are complete

    @Test
    fun castString() = with(invokeLine("File(\"readme.md\").toString()")) {
        Assert.assertTrue(this is KrashValueString)
        (this as KrashValueString).let {
            Assert.assertEquals("readme.md", it.getValue())
        }
    }

    @Test
    fun create() = with(invokeLine("File(\"readme.md\")")) {
        Assert.assertTrue(this is KrashValueObject)
    }

    @Test
    fun files() = System.getProperty("user.dir").let {cwd ->
        with(invokeLine("File(\"$cwd\").files()")) {
            Assert.assertTrue(this is KrashValueArray)
            (this as KrashValueArray).let {
                File(cwd).listFiles().let {fileList ->
                    Assert.assertEquals(fileList.size, it.getSize())
                }
            }
        }
    }

    @Test
    fun isDirectory() {

        // True
        with(invokeLine("File(\"${System.getProperty("user.dir")}\").isDirectory")) {
            Assert.assertTrue(this is KrashValueBoolean)
            (this as KrashValueBoolean).let {
                Assert.assertEquals(true, it.isTrue())
            }
        }

        // False
        with(invokeLine("File(\"readme.md\").isDirectory")) {
            Assert.assertTrue(this is KrashValueBoolean)
            (this as KrashValueBoolean).let {
                Assert.assertEquals(false, it.isTrue())
            }
        }
    }

    @Test
    fun path() = with(invokeLine("File(\"readme.md\").path")) {
        Assert.assertTrue(this is KrashValueString)
        (this as KrashValueString).let {
            Assert.assertEquals("readme.md", it.getValue())
        }
    }
    // NOTE: should path not provide absolute path instead of relative / supplied?

}