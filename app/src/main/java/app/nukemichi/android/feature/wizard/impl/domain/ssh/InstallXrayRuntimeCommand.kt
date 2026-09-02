package app.nukemichi.android.feature.wizard.impl.domain.ssh

import app.nukemichi.android.core.ssh.ShellHost
import app.nukemichi.android.core.ssh.command.BashScriptCommand
import app.nukemichi.android.core.ssh.model.CommandResult
import app.nukemichi.android.feature.wizard.impl.domain.model.PackageManager

internal class InstallXrayRuntimeCommand(
    packageManager: PackageManager,
    releaseAsset: ReleaseAsset,
) : BashScriptCommand<Unit> {
    private val assetName = ShellHost.of(releaseAsset.name)

    // Not ShellHost: a 64-char digest exceeds the 63-char label limit that guard enforces. Safety
    // comes from it being a compile-time literal below, never anything the server or network says.
    private val assetSha256: String = releaseAsset.sha256

    // Not ShellHost: this is assembled shell syntax (`;`/`&&`), not a single token. Its safety
    // comes from every argument to installCommand(...) above being a compile-time literal.
    private val installDependencies: String = packageManager.installCommand("ca-certificates", "curl", "unzip", "openssl")

    override val script: String = $$"""
        set -eu
        test "$(id -u)" -eq 0 || { echo 'Deployment requires a root SSH user.' >&2; exit 1; }

        $$installDependencies

        workdir="$(mktemp -d)"
        trap 'rm -rf "$workdir"' EXIT
        curl --fail --location --retry 3 --silent --show-error \
          "https://github.com/XTLS/Xray-core/releases/download/$$XRAY_VERSION/$$assetName" \
          --output "$workdir/xray.zip"
        echo "$$assetSha256  $workdir/xray.zip" | sha256sum -c - \
          || { echo 'Xray-core archive failed SHA-256 verification - refusing to install.' >&2; exit 1; }
        unzip -qo "$workdir/xray.zip" -d "$workdir/xray"
        install -Dm755 "$workdir/xray/xray" /usr/local/bin/xray
        install -d -m 0755 /usr/local/etc/xray
    """.trimIndent()

    override fun parseOutput(result: CommandResult) = Unit

    /** A release asset paired with the digest it must hash to, so the two cannot drift apart. */
    data class ReleaseAsset(val name: String, val sha256: String)

    companion object {
        private const val XRAY_VERSION = "v26.7.28"

        // Digests are the upstream release's own published .dgst values (SHA2-256), verified
        // against a locally computed hash of the downloaded asset. Update together with
        // XRAY_VERSION - a version bump with a stale digest fails the install, by design.
        fun releaseAssetFor(architecture: String): ReleaseAsset = when (architecture) {
            "64" -> ReleaseAsset(
                name = "Xray-linux-64.zip",
                sha256 = "8195d909f1109b8f3d99eefe401a3c451d7bf4af71f24d3815420f77e5dd2a40",
            )

            "arm64-v8a" -> ReleaseAsset(
                name = "Xray-linux-arm64-v8a.zip",
                sha256 = "f5698bb218ada3b4022db26fafc39601c5f53b46b19eb76c9616325985807501",
            )

            else -> error("Unsupported server architecture: $architecture")
        }
    }
}
