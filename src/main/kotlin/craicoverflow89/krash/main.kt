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

fun main(args: Array<String>) = when {

    // Shell Mode
    args.isEmpty() -> loadShell()

    // Flag Mode
    args[0].startsWith("-") -> loadFlags(args[0].let {
        it.substring(1, it.length)
    })

    // Script Mode
    else -> loadScript(args[0])
    // NOTE: this will completely ignore anything after script path (flags?)
}

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
        println("Could not find script!")
        exitProcess(-1)
    }

    // Invalid File
    if(scriptFile.extension != "krash") {
        println("Must be a krash script!")
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
        print("${cwd.replace("\\", "/")} \$ ")
        // NOTE: need to get cwd from runtime when cd is added

        // Read Line
        val input = readLine() ?: continue

        // Exit Command
        if(input == "exit") break
        // NOTE: should probably invoke runtime.exit(0) here now

        // Empty Command
        if(input.isEmpty()) continue

        // Invoke Command
        parse(input).invoke(runtime)
    }

    // Shell Done
    println("")
}

fun printInfo() {
    println("")
    println("Krash Project")
    println("Version $KRASH_VERSION")
    println("")
}