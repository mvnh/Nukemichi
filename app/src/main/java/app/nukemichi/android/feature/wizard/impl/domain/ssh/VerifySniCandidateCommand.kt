package app.nukemichi.android.feature.wizard.impl.domain.ssh

import app.nukemichi.android.core.ssh.ShellSafe
import app.nukemichi.android.core.ssh.command.BashScriptCommand
import app.nukemichi.android.core.ssh.model.CommandResult

internal class VerifySniCandidateCommand(domain: String) : BashScriptCommand<Boolean> {
    private val domain = ShellSafe.of(domain)

    override val script: String = $$"""
        set -eu
        ok=1
        for _ in 1 2 3; do
            code="$(curl -s -o /dev/null -w '%{http_code}' --max-time 4 "https://$$domain/" || echo 000)"
            if [ "$code" = "000" ]; then
                ok=0
                break
            fi
        done
        echo "$ok"
    """.trimIndent()

    override fun parseOutput(result: CommandResult): Boolean = result.stdout.trim() == "1"
}
