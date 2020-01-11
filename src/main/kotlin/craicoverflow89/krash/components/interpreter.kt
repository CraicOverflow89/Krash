package craicoverflow89.krash.components

import craicoverflow89.krash.KrashException
import craicoverflow89.krash.parser.KrashLexer
import craicoverflow89.krash.parser.KrashParser
import org.antlr.v4.runtime.*

class KrashInterpreter {

    companion object {

        fun parseCommand(input: String) = parseText(input).line().result

        fun parseScript(input: String) = parseText(input).script().result

        private fun parseText(input: String): KrashParser {

            // Create Listener
            return KrashInterpreterListener().let {

                // Create Lexer
                val lexer = KrashLexer(ANTLRInputStream(input)).apply {
                    removeErrorListeners()
                    addErrorListener(it)
                }

                // Create Parser
                KrashParser(CommonTokenStream(lexer)).apply {
                    removeErrorListeners()
                    addErrorListener(it)
                }
            }
        }

    }

}

class KrashInterpreterException(posLine: Int, posChar: Int, message: String?): KrashException(StringBuffer().apply {
    append("Syntax error at $posLine:$posChar")
    message?.let {
        append(": $it")
    }
}.toString())

class KrashInterpreterListener: BaseErrorListener() {

    override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
        throw KrashInterpreterException(line, charPositionInLine, msg)
    }

}