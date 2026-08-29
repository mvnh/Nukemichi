package app.nukemichi.android.feature.wizard.impl.domain.model

// Applied to every deployment log line at the one place they all funnel through (the UI reducer),
// so a command that streams raw stdout can't leak a key/UUID into the visible terminal just because
// nobody remembered to scrub that specific command's output. GenerateXrayServerSecretsCommand's own
// script already never echoes xray's raw key material — it captures it into shell variables and
// only prints it back through the KEY=value lines this matches — but this stays independent of that
// command's own care, on the same "don't trust one call site to get it right forever" reasoning as
// everywhere else secrets get handled in this app.
private val KEY_VALUE_PATTERN = Regex("\\b(UUID|PRIVATE_KEY|PUBLIC_KEY|SHORT_ID)=\\S+")
private val UUID_PATTERN = Regex("\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b")

internal fun redactSecrets(line: String): String = line
    .replace(KEY_VALUE_PATTERN) { match -> "${match.groupValues[1]}=[REDACTED]" }
    .replace(UUID_PATTERN, "[REDACTED]")
