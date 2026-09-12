import java.io.File
import java.net.URI
import java.time.YearMonth
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

/**
 * Maintainer-only, never wired into preBuild: rebuilds app/src/main/assets/geoip.dat from a fresh
 * DB-IP country-lite snapshot. Needs Go on PATH and network access. See tools/geoip-dat/README.md
 * for why this can't be an ordinary pinned download, and for the steps to run after it.
 */
abstract class RegenerateGeoipDatTask : DefaultTask() {

    @get:Input
    abstract val geoipGeneratorCommit: Property<String>

    @get:InputFile
    abstract val generatorConfig: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun regenerate() {
        val workDir = File(temporaryDir, "v2fly-geoip").apply {
            deleteRecursively()
            mkdirs()
        }

        logger.lifecycle("Cloning v2fly/geoip @ ${geoipGeneratorCommit.get()}")
        execOperations.exec {
            commandLine("git", "clone", "--quiet", "https://github.com/v2fly/geoip.git", workDir.absolutePath)
        }
        execOperations.exec {
            workingDir = workDir
            commandLine("git", "checkout", "--quiet", geoipGeneratorCommit.get())
        }

        val yearMonth = YearMonth.now()
        val dbIpUrl = "https://download.db-ip.com/free/dbip-country-lite-$yearMonth.mmdb.gz"
        logger.lifecycle("Downloading DB-IP country-lite data for $yearMonth")
        val dbIpGz = File(workDir, "dbip.mmdb.gz")
        URI(dbIpUrl).toURL().openStream().use { input ->
            dbIpGz.outputStream().use { output -> input.copyTo(output) }
        }
        val dbIpDir = File(workDir, "db-ip").apply { mkdirs() }
        val dbIpMmdb = File(dbIpDir, "dbip-country-lite.mmdb")
        GZIPInputStream(dbIpGz.inputStream()).use { input ->
            dbIpMmdb.outputStream().use { output -> input.copyTo(output) }
        }

        val configTarget = File(workDir, "nukemichi-config.json")
        generatorConfig.get().asFile.copyTo(configTarget, overwrite = true)

        logger.lifecycle("Running the geoip generator")
        execOperations.exec {
            workingDir = workDir
            commandLine("go", "run", ".", "-c", configTarget.absolutePath)
        }

        val generated = File(workDir, "nukemichi-output/geoip.dat")
        check(generated.exists()) { "geoip generator did not produce nukemichi-output/geoip.dat" }

        val target = outputFile.get().asFile
        target.parentFile.mkdirs()
        generated.copyTo(target, overwrite = true)

        logger.lifecycle(
            "Wrote ${target.path}\n" +
                "New SHA-256: ${ChecksumUtil.sha256Hex(target)}\n" +
                "Update GEOIP_DAT_SHA256 in app/build.gradle.kts to this value, then commit both " +
                "together (see tools/geoip-dat/README.md).",
        )
    }
}
