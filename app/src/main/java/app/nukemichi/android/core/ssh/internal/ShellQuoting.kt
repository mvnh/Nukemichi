package app.nukemichi.android.core.ssh.internal

internal fun renderCommand(command: String, args: List<String>): String = buildString {
    append(command)
    args.forEach { argument ->
        append(' ')
        append(argument.shellQuote())
    }
}

private fun String.shellQuote(): String = "'${replace("'", "'\\''")}'"
