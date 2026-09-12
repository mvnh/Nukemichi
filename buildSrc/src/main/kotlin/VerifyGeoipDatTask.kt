import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

/**
 * Verifies the vendored app/src/main/assets/geoip.dat against a pinned SHA-256, the same way
 * DownloadLibV2rayTask (in :app) verifies a download - except there is nothing to download here.
 * geoip.dat is regenerated rarely and by hand (see RegenerateGeoipDatTask and
 * tools/geoip-dat/README.md), then committed; this task's only job on every normal build is to
 * catch the committed file and the pinned checksum drifting apart.
 */
abstract class VerifyGeoipDatTask : DefaultTask() {

    @get:InputFile
    abstract val geoipDat: RegularFileProperty

    @get:Input
    abstract val expectedSha256: Property<String>

    @TaskAction
    fun verify() {
        val file = geoipDat.get().asFile
        val actual = ChecksumUtil.sha256Hex(file)
        if (actual != expectedSha256.get()) {
            throw GradleException(
                "app/src/main/assets/geoip.dat does not match the pinned GEOIP_DAT_SHA256 " +
                    "(expected ${expectedSha256.get()}, got $actual). Either the file was edited " +
                    "without updating the constant, or it needs regenerating: see " +
                    "tools/geoip-dat/README.md."
            )
        }
        logger.info("geoip.dat matches the pinned SHA-256")
    }
}
