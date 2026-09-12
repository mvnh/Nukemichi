import java.io.File
import java.net.URI
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Downloads dlc.dat from v2fly/domain-list-community and stages it as geosite.dat - the compiled
 * domain-category database Xray's `geosite:` routing rules read at runtime (see
 * XrayRoutingFactory in :app). Unlike geoip.dat, this one *can* be pinned forever the same way
 * libv2ray.aar is: it's an immutable GitHub release asset, not a source someone else rotates out
 * from under a fixed URL.
 */
abstract class DownloadGeositeDatTask : DefaultTask() {

    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val sha256: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun download() {
        val target = outputFile.get().asFile

        if (target.exists()) {
            if (ChecksumUtil.sha256Hex(target) == sha256.get()) {
                logger.info("geosite.dat already staged and matches the pinned SHA-256")
                return
            }
            logger.warn("Staged geosite.dat does not match the pinned SHA-256 - discarding it and downloading again.")
            target.delete()
        }

        val url = "https://github.com/v2fly/domain-list-community/releases/download/${version.get()}/dlc.dat"
        val tempFile = File(temporaryDir, "geosite.dat")
        logger.lifecycle("Downloading geosite.dat (domain-list-community ${version.get()})")

        URI(url).toURL().openStream().use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }
        val actual = ChecksumUtil.sha256Hex(tempFile)
        if (actual != sha256.get()) {
            tempFile.delete()
            throw GradleException("geosite.dat checksum mismatch: expected ${sha256.get()} but got $actual.")
        }
        target.parentFile.mkdirs()
        tempFile.copyTo(target, overwrite = true)
        logger.lifecycle("geosite.dat staged at ${target.path}")
    }
}
