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

fun main(args: Array<String>) {

    // Shell Mode
    if(args.isEmpty()) loadShell()

    // Script Mode
    else loadScript(args[0])
    // NOTE: this will completely ignore anything after script path (flags?)
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
    scriptData.invoke()
}

fun loadShell() {

    // Define Paths
    val cwd = System.getProperty("user.dir") ?: ""

    // Shell Info
    println("")
    println("Krash Project")
    println("Version $KRASH_VERSION")
    println("")

    // Create Runtime
    val runtime = KrashRuntime()

    // Command Parser
    val parse = fun(input: String): KrashCommand {
        val lexer = KrashLexer(ANTLRInputStream(input))
        val parser = KrashParser(CommonTokenStream(lexer))
        return parser.line().result
    }

    // Shell Loop
    while(true) {

        // Print Line
        print("${cwd.replace("\\", "/")} \$ ")

        // Read Line
        val input = readLine() ?: ""

        // Exit Command
        if(input == "exit") break

        // Empty Command
        if(input.isEmpty()) continue

        // Invoke Command
        //parseCommand(input)

        // TEMP DEBUG
        parse(input).let {
            //println(it)
            it.invoke(runtime)
        }
    }

    // Shell Done
    println("")
}