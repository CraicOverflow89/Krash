package craicoverflow89.krash

import craicoverflow89.krash.components.KrashCommand
import craicoverflow89.krash.components.KrashRuntime
import craicoverflow89.krash.parser.KrashLexer
import craicoverflow89.krash.parser.KrashParser
import java.io.File
import kotlin.system.exitProcess
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream

// Define Version
val KRASH_VERSION = "ALPHA"

fun main() {
    loadScript("src/main/resources/arrays.krash")
    //loadScript("src/main/resources/comments.krash")
    //loadScript("src/main/resources/maps.krash")
    //loadScript("src/main/resources/references.krash")

    // NOTE: multiline comment is currently taking all text after /**
    //loadScript("src/main/resources/test1.krash")
}

/*fun main(args: Array<String>) = when {

    // Shell Mode
    args.isEmpty() -> loadShell()

    // Flag Mode
    args[0].startsWith("-") -> loadFlags(args[0].let {
        it.substring(1, it.length)
    })

    // Script Mode
    else -> loadScript(args[0])
    // NOTE: this will completely ignore anything after script path (flags?)
}*/

fun loadFlags(flags: String) {

    // Version Info
    if(listOf("version", "v").contains(flags)) printInfo()
}

fun loadScript(scriptPath: String) {

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

    // Parse File
    val scriptData = scriptFile.readText().let {
        val lexer = KrashLexer(ANTLRInputStream(it))
        val parser = KrashParser(CommonTokenStream(lexer))
        parser.script().result
    }

    // Invoke Script
    scriptData.invoke(cwd)
}

fun loadShell() {

    // Define Paths
    val cwd = System.getProperty("user.dir") ?: ""

    // Shell Info
    printInfo()

    // Create Runtime
    val runtime = KrashRuntime(cwd)

    // Parse Command
    val parse = fun(input: String): KrashCommand {
        val lexer = KrashLexer(ANTLRInputStream(input))
        val parser = KrashParser(CommonTokenStream(lexer))
        return parser.line().result
    }

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
        try {parse(input).invoke(runtime)}

        // Error Handling
        catch(ex: RuntimeException) {
            KrashRuntime.println("ERROR: ${ex.message}")
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