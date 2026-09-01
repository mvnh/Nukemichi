package app.nukemichi.android.feature.wizard.impl.domain.model

// Applied in the UI reducer, the one place every deployment log line passes through, so no single
// command has to remember to scrub its own stdout.
private val KEY_VALUE_PATTERN = Regex("\\b(UUID|PRIVATE_KEY|PUBLIC_KEY|SHORT_ID)=\\S+")
private val UUID_PATTERN = Regex("\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b")

internal fun redactSecrets(line: String): String = line
    .replace(KEY_VALUE_PATTERN) { match -> "${match.groupValues[1]}=[REDACTED]" }
    .replace(UUID_PATTERN, "[REDACTED]")
