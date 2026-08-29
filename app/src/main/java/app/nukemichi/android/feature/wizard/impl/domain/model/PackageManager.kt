package app.nukemichi.android.feature.wizard.impl.domain.model

internal enum class PackageManager(val token: String) {
    APT("apt"),
    DNF("dnf"),
    YUM("yum"),
    PACMAN("pacman"),
    APK("apk");

    fun installCommand(vararg packages: String): String {
        val names = packages.joinToString(" ")
        return when (this) {
            APT -> "export DEBIAN_FRONTEND=noninteractive; apt-get update -qq && apt-get install -y -qq $names"
            DNF -> "dnf install -y -q $names"
            YUM -> "yum install -y -q $names"
            PACMAN -> "pacman -Sy --noconfirm $names"
            APK -> "apk add --no-cache $names"
        }
    }

    companion object {
        fun fromToken(token: String): PackageManager? = entries.firstOrNull { it.token == token.trim() }
    }
}
