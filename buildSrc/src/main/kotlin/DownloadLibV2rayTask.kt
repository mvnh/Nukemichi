import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI

abstract class DownloadLibV2rayTask : DefaultTask() {

    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val sha256: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun download() {
        val target = outputFile.get().asFile

        // Re-verified rather than trusted for existing: this path is restored from the Actions
        // cache before the task runs, so skipping an already-present file meant the pinned digest
        // was never checked on any CI build, releases included. A local build/ is no better.
        if (target.exists()) {
            if (ChecksumUtil.sha256Hex(target) == sha256.get()) {
                logger.info("libv2ray.aar already staged and matches the pinned SHA-256")
                return
            }
            logger.warn("Staged libv2ray.aar does not match the pinned SHA-256, discarding it and downloading again.")
            target.delete()
        }

        val url = "https://github.com/2dust/AndroidLibXrayLite/releases/download/${version.get()}/libv2ray.aar"
        val tempFile = File(temporaryDir, "libv2ray.aar")
        logger.lifecycle("Downloading libv2ray.aar ${version.get()}")

        URI(url).toURL().openStream().use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }
        val actual = ChecksumUtil.sha256Hex(tempFile)
        if (actual != sha256.get()) {
            tempFile.delete()
            throw GradleException("libv2ray.aar checksum mismatch: expected ${sha256.get()} but got $actual.")
        }
        target.parentFile.mkdirs()
        tempFile.copyTo(target, overwrite = true)
        logger.lifecycle("libv2ray.aar staged at ${target.path}")
    }
}
