package app.nukemichi.android.feature.wizard.impl.domain.ssh

import app.nukemichi.android.core.ssh.command.BashScriptCommand
import app.nukemichi.android.core.ssh.model.CommandResult

/**
 * Asks a GeoIP service which country the VPS is in. The request leaves from the server itself, so the
 * phone never tells a third party which server it uses. The answer is untrusted network input: anything
 * other than a bare ISO 3166-1 alpha-2 code parses to null, and the script always exits 0 so a failed
 * lookup never reads as a failed deployment.
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
