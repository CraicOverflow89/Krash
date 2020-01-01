package craicoverflow89.krash

import java.io.File
import kotlin.system.exitProcess

// Define Version
val KRASH_VERSION = "ALPHA"

fun main(args: Array<String>) {

    // Shell Mode
    if(args.isEmpty()) loadShell()

    // Script Mode
    else loadScript(args[0])
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

    // Load File
    // NOTE: iterate lines and invoke commands
}

fun loadShell() {

    // Define Paths
    val cwd = System.getProperty("user.dir") ?: ""

    // Shell Info
    println("")
    println("Krash Project")
    println("Version $KRASH_VERSION")
    println("")

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
        // NOTE: invoke command based on input content

        // TEMP DEBUG
        println(input)
    }

    // Shell Done
    println("")
}