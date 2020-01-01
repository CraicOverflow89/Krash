package craicoverflow89.krash.components

interface KrashCommand

class KrashCommandDeclare(private val ref: KrashReference, private val value: KrashValue): KrashCommand

class KrashCommandInvoke(private val method: KrashMethod, private val argumentList: List<KrashCommandInvokeArgument>): KrashCommand

class KrashCommandInvokeArgument(private val value: KrashValue)