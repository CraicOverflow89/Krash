package craicoverflow89.krash.system.tests

import craicoverflow89.krash.system.KrashFileSystem
import craicoverflow89.krash.system.KrashSystemTest
import org.junit.Assert
import org.junit.Test

class KrashFileSystemTest: KrashSystemTest() {

    @Test
    fun isAbsolutePath() {

        // Test Values
        {expected: Boolean, values: List<String> -> values.forEach {
            Assert.assertEquals(expected, KrashFileSystem.isAbsolutePath(it))
        }}.let {

            // Nix Paths
            it(true, listOf("/", "/bin/", "/home/james/", "/temp.txt"))

            // Windows Paths
            it(true, listOf("c:\\windows", "c:\\", "c:/", "c:/windows", "c:/temp.txt"))

            // Relative Paths
            it(false, listOf(".", "directory", "./directory", "directory/new", "directory/new/temp.txt"))
        }
    }

}