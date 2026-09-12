package app.nukemichi.android.feature.wizard.impl.domain.ssh

import app.nukemichi.android.core.ssh.command.BashScriptCommand
import app.nukemichi.android.core.ssh.model.CommandResult

/**
 * The request leaves from the VPS, so the phone never tells a third party which server it uses. The
 * answer is untrusted network input, and the script always exits 0: a failed lookup is not a failed
 * deployment.
 */
internal class DetectServerCountryCommand : BashScriptCommand<String?> {
    override val script: String = $$"""
        for url in https://ipinfo.io/country https://ifconfig.co/country-iso; do
          if code="$(curl --fail --silent --max-time 5 "$url")" && [ -n "$code" ]; then
            printf '%s\n' "$code"
            exit 0
          fi
        done
        exit 0
    """.trimIndent()

    override fun parseOutput(result: CommandResult): String? = result.stdout.trim().takeIf(ISO_ALPHA_2::matches)

    private companion object {
        val ISO_ALPHA_2 = Regex("^[A-Z]{2}$")
    }
}
