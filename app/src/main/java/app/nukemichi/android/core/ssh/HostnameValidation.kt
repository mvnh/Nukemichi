package app.nukemichi.android.core.ssh

fun isSafeHostname(value: String): Boolean =
    value.isNotBlank() && value.length <= MAX_HOSTNAME_LENGTH && HOSTNAME_REGEX.matches(value)

private const val MAX_HOSTNAME_LENGTH = 253

private val HOSTNAME_REGEX = Regex(
    "^(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\\.(?!-)[A-Za-z0-9-]{1,63}(?<!-))*$"
)
