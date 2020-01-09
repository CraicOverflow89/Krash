package craicoverflow89.krash

import craicoverflow89.krash.components.KrashInterpreter
import craicoverflow89.krash.components.KrashRuntime
import java.io.File
import kotlin.system.exitProcess

// Define Version
val KRASH_VERSION = "ALPHA"

fun main() {
    //loadScript("src/main/resources/functions.krash")
    //loadScript("src/main/resources/maps.krash")
    //loadScript("src/main/resources/numbers.krash")
    //loadScript("src/main/resources/references.krash")
    //loadScript("src/main/resources/script.krash", listOf("-user", "James"))
    //loadScript("src/main/resources/structures.krash")

    // NOTE: multiline comment is currently taking all text
    //       looks like single comments within multiline comments are causing errors
    //loadScript("src/main/resources/test1.krash")

    // NOTE: multiple indexes for update and increment operations are not working
    //loadScript("src/main/resources/test2.krash")

    // TEST
    //loadScript("src/main/resources/test3.krash")
    loadScript("src/main/resources/test4.krash")
}

/*fun main(args: Array<String>) = when {

    // Shell Mode
    args.isEmpty() -> loadShell()

    // Flag Mode
    args[0].startsWith("-") -> loadFlags(args[0].let {
        it.substring(1, it.length)
    })

    // Script Mode
    else -> loadScript(args[0], args.let {

        // Script Arguments
        if(it.size > 1) it.copyOfRange(1, args.size).toList()

        // No Arguments
        else listOf()
    })
}*/

fun loadFlags(flags: String) {

    // Version Info
    if(listOf("version", "v").contains(flags)) printInfo()
}

fun loadScript(scriptPath: String, scriptArgs: List<String> = listOf()) {

    // Define Paths
    val cwd = System.getProperty("user.dir") ?: ""
    val scriptFile = File("$cwd/$scriptPath")

    // Missing File
    if(!scriptFile.exists()) {
        KrashRuntime.println("Could not find script!")
        exitProcess(-1)
    }

    // Invalid File
    if(scriptFile.extension != "krash") {
        KrashRuntime.println("Must be a krash script!")
        exitProcess(-1)
    }

    // Invoke Script
    KrashInterpreter.parseScript(scriptFile.readText()).invoke(cwd, scriptFile.absolutePath, scriptArgs)
}

fun loadShell() {

    // Define Paths
    val cwd = System.getProperty("user.dir") ?: ""

    // Shell Info
    printInfo()

    // Create Runtime
    val runtime = KrashRuntime(cwd)

    // Shell Loop
    while(true) {

        // Print Line
        print("${KrashRuntime.cwd()} \$ ")

        // Read Line
        var input = readLine() ?: continue

        // Exit Command
        if(input == "exit") break

        // Empty Command
        if(input.isEmpty()) continue

        // Open Block
        if(input.endsWith("{")) {

            // Indent Spacing
            val indentSpace = " ".repeat(KrashRuntime.cwd().length)

            // Create Buffer
            val buffer = arrayListOf(input)

            // Block Loop
            while(true) {

                // Print Line
                print("$indentSpace \$ ")

                // Read Line
                val input = readLine() ?: continue

                // Empty Command
                if(input.isEmpty()) continue

                // Append Command
                buffer.add(input)

                // Close Block
                if(input.startsWith("}")) break
                // NOTE: need to make this far more intelligent
            }

            // Update Input
            input = buffer.joinToString("\n")
        }

        // Invoke Command
        try {KrashInterpreter.parseCommand(input).invoke(runtime)}

        // Error Handling
        catch(ex: KrashException) {
            KrashRuntime.error(ex.message())
        }
    }

    // Shell Done
    KrashRuntime.println("")
}

fun printInfo() {
    KrashRuntime.println("")
    KrashRuntime.println("Krash Project")
    KrashRuntime.println("Version $KRASH_VERSION")
    KrashRuntime.println("")
}

abstract class KrashException(message: String): Exception(message) {

    fun message(): String =  message ?: "Unknown Exception"

}