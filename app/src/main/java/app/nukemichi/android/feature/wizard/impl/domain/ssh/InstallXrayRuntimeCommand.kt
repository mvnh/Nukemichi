package app.nukemichi.android.feature.wizard.impl.domain.ssh

import app.nukemichi.android.core.ssh.command.BashScriptCommand
import app.nukemichi.android.core.ssh.model.CommandResult
import app.nukemichi.android.feature.wizard.impl.domain.model.PackageManager

internal class InstallXrayRuntimeCommand(
    packageManager: PackageManager,
    releaseAsset: String,
) : BashScriptCommand<Unit> {
    private val installDependencies: String = packageManager.installCommand("ca-certificates", "curl", "unzip", "openssl")

    override val script: String = $$"""
        set -eu
        test "$(id -u)" -eq 0 || { echo 'Deployment requires a root SSH user.' >&2; exit 1; }

        $$installDependencies

        workdir="$(mktemp -d)"
        trap 'rm -rf "$workdir"' EXIT
        curl --fail --location --retry 3 --silent --show-error \
          "https://github.com/XTLS/Xray-core/releases/download/$$XRAY_VERSION/$$releaseAsset" \
          --output "$workdir/xray.zip"
        unzip -qo "$workdir/xray.zip" -d "$workdir/xray"
        install -Dm755 "$workdir/xray/xray" /usr/local/bin/xray
        install -d -m 0755 /usr/local/etc/xray
    """.trimIndent()

    override fun parseOutput(result: CommandResult) = Unit

    companion object {
        private const val XRAY_VERSION = "v26.7.28"

        fun releaseAssetFor(architecture: String): String = when (architecture) {
            "64" -> "Xray-linux-64.zip"
            "arm64-v8a" -> "Xray-linux-arm64-v8a.zip"
            else -> error("Unsupported server architecture: $architecture")
        }
    }
}
