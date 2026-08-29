package app.nukemichi.android.feature.wizard.impl.domain.ssh

import app.nukemichi.android.core.ssh.command.BashScriptCommand
import app.nukemichi.android.core.ssh.model.CommandResult
import app.nukemichi.android.feature.wizard.impl.domain.model.PackageManager

internal class DetectPackageManagerCommand : BashScriptCommand<PackageManager> {
    override val script: String = """
        set -eu
        if command -v apt-get >/dev/null 2>&1; then echo apt
        elif command -v dnf >/dev/null 2>&1; then echo dnf
        elif command -v yum >/dev/null 2>&1; then echo yum
        elif command -v pacman >/dev/null 2>&1; then echo pacman
        elif command -v apk >/dev/null 2>&1; then echo apk
        else echo none; exit 1
        fi
    """.trimIndent()

    override fun parseOutput(result: CommandResult): PackageManager =
        requireNotNull(PackageManager.fromToken(result.stdout)) {
            "No supported package manager (apt/dnf/yum/pacman/apk) found on the server."
        }
}
