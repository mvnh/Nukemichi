package app.nukemichi.android.feature.wizard.impl.domain.ssh

import app.nukemichi.android.core.ssh.ShellHost
import app.nukemichi.android.core.ssh.command.BashScriptCommand
import app.nukemichi.android.core.ssh.isSafeHostname
import app.nukemichi.android.core.ssh.model.CommandResult

internal class ScanSniCommand(architecture: String) : BashScriptCommand<List<String>> {
    private val asset = assetFor(architecture)
    private val assetName = ShellHost.of(asset.name)

    // Not ShellHost: a 64-char digest exceeds the 63-char label limit that guard enforces. Safety
    // comes from it being a compile-time literal below, never anything the server or network says.
    private val assetSha256: String = asset.sha256

    override val script: String = $$"""
        set -eu
        workdir="$(mktemp -d)"
        trap 'rm -rf "$workdir"' EXIT

        curl --fail --location --retry 3 --silent --show-error \
          "https://github.com/XTLS/RealiTLScanner/releases/download/$$SCANNER_VERSION/$$assetName" \
          --output "$workdir/scanner"
        echo "$$assetSha256  $workdir/scanner" | sha256sum -c - \
          || { echo 'RealiTLScanner binary failed SHA-256 verification - refusing to run.' >&2; exit 1; }
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

    /** A release asset and the digest it must hash to — paired so the two can't drift apart. */
    data class ScannerAsset(val name: String, val sha256: String)

    companion object {
        private const val SCANNER_VERSION = "v0.2.3"

        private val FEASIBLE_DOMAIN_REGEX = Regex("""feasible=true.*?\bdomain=(\S+)""")

        // Upstream publishes no checksums for these assets, so unlike Xray-core's own .dgst these
        // digests are computed from the downloaded binaries and pinned here. Update together with
        // SCANNER_VERSION - a version bump with a stale digest fails the scan, by design.
        private fun assetFor(architecture: String): ScannerAsset = when (architecture) {
            "64" -> ScannerAsset(
                name = "RealiTLScanner-linux-amd64",
                sha256 = "a55595446de9f1c2e6c5c3cd766a7320a11115947df48f101749bb62c8055592",
            )

            "arm64-v8a" -> ScannerAsset(
                name = "RealiTLScanner-linux-arm64",
                sha256 = "27bdd3e53d4391c66c8df3391d3c3fb5eb2dc356125f2fb33ac58fcaaf8f88b3",
            )

            else -> error("Unsupported server architecture: $architecture")
        }
    }
}
