package app.nukemichi.android.feature.wizard.impl.domain.ssh

import app.nukemichi.android.core.ssh.ShellSafe
import app.nukemichi.android.core.ssh.command.BashScriptCommand
import app.nukemichi.android.core.ssh.isSafeHostname
import app.nukemichi.android.core.ssh.model.CommandResult

internal class ScanSniCommand(architecture: String) : BashScriptCommand<List<String>> {
    private val asset = ShellSafe.of(assetFor(architecture))

    override val script: String = $$"""
        set -eu
        workdir="$(mktemp -d)"
        trap 'rm -rf "$workdir"' EXIT

        curl --fail --location --retry 3 --silent --show-error \
          "https://github.com/XTLS/RealiTLScanner/releases/download/$$SCANNER_VERSION/$$asset" \
          --output "$workdir/scanner"
        chmod +x "$workdir/scanner"

        ip="$(curl --fail --silent --show-error https://api.ipify.org)"
        subnet="$(printf '%s' "$ip" | awk -F. '{print $1"."$2"."$3".0/24"}')"

        "$workdir/scanner" -addr "$subnet" -thread 16 -timeout 5
    """.trimIndent()

    override fun parseOutput(result: CommandResult): List<String> =
        FEASIBLE_DOMAIN_REGEX.findAll(result.stdout)
            .map { it.groupValues[1] }
            .filter(::isSafeHostname)
            .distinct()
            .toList()

    companion object {
        private const val SCANNER_VERSION = "v0.2.3"

        private val FEASIBLE_DOMAIN_REGEX = Regex("""feasible=true.*?\bdomain=(\S+)""")

        private fun assetFor(architecture: String): String = when (architecture) {
            "64" -> "RealiTLScanner-linux-amd64"
            "arm64-v8a" -> "RealiTLScanner-linux-arm64"
            else -> error("Unsupported server architecture: $architecture")
        }
    }
}
