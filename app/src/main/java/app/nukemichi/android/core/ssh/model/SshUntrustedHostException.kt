package app.nukemichi.android.core.ssh.model

class SshUntrustedHostException(
    val fingerprint: String
) : Exception("Host key is not trusted. Fingerprint: $fingerprint")
