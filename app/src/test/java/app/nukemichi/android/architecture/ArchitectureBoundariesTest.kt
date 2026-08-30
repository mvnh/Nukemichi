package app.nukemichi.android.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * The whole app lives in one Gradle module, so `internal` only blocks access from *outside the
 * module* - there is no outside, so it currently enforces nothing. These tests are the substitute
 * for the module boundaries these packages are already written as if they had:
 *
 *  1. core.<module> exposes only abstractions (interfaces, abstract/sealed classes, plain data
 *     types) outside its own .internal/.di - concrete behavior lives in .internal.
 *  2. feature.<name> exposes only *Key navigation keys at its root - everything else lives in
 *     .impl.
 *  3. every root *Key has a Destination<Key> registered somewhere under that feature's .impl, or
 *     MainActivity's lookup fails at runtime instead of at build time.
 *  4. feature.<name>.impl and core.<module>.internal are never reached from outside their own
 *     module (core.<module>.di is the one sanctioned exception, since binding an interface to its
 *     impl requires naming the impl).
 *  5. clean architecture inside a feature: domain never depends on UI structures (Compose,
 *     core.ui), ui never reaches past domain straight into core.*.internal, and core never
 *     depends on feature at all.
 */
class ArchitectureBoundariesTest {

    private val projectFiles: List<KoFileDeclaration> =
        Konsist.scopeFromProject().files.filter { it.path.contains("/main/java/") }

    private val projectClasses: List<KoClassDeclaration> = projectFiles.flatMap { it.classes() }

    private val corePackageRegex = Regex("""^app\.nukemichi\.android\.core\.([a-zA-Z0-9]+)(\.(.+))?$""")
    private val featurePackageRegex = Regex("""^app\.nukemichi\.android\.feature\.([a-zA-Z0-9]+)(\.(.+))?$""")

    @Test
    fun `core packages expose only abstractions outside internal and di`() {
        val violations = projectClasses.filter { clazz ->
            val pkg = clazz.packagee?.name ?: return@filter false
            val match = corePackageRegex.find(pkg) ?: return@filter false
            val rest = match.groupValues[3]
            val isInternalOrDi = rest == "internal" || rest.startsWith("internal.") ||
                rest == "di" || rest.startsWith("di.")
            if (isInternalOrDi) return@filter false

            val isExceptionType = clazz.name.endsWith("Exception") || clazz.name.endsWith("Error")

            !(clazz.hasAbstractModifier || clazz.hasDataModifier || clazz.hasEnumModifier ||
                clazz.hasValueModifier || clazz.hasSealedModifier || clazz.hasAnnotationModifier ||
                isExceptionType)
        }

        if (violations.isNotEmpty()) {
            fail(
                "core.<module> exposes only abstractions and plain data types outside " +
                    ".internal/.di - a concrete behavioral class belongs in .internal:\n" +
                    violations.describeClasses()
            )
        }
    }

    @Test
    fun `feature root packages expose only navigation keys`() {
        val violations = rootFeatureFiles().filterNot { it.fileName().endsWith("Key.kt") }

        if (violations.isNotEmpty()) {
            fail(
                "feature.<name> root may only contain *Key navigation keys - everything else " +
                    "belongs in feature.<name>.impl:\n" + violations.describeFiles()
            )
        }
    }

    @Test
    fun `every feature navigation key has a registered Destination`() {
        val rootKeyFiles = rootFeatureFiles().filter { it.fileName().endsWith("Key.kt") }

        val violations = rootKeyFiles.filterNot { keyFile ->
            val featurePkg = keyFile.packagee?.name ?: return@filterNot false
            val keyName = keyFile.fileName().removeSuffix(".kt")
            projectClasses.any { clazz ->
                val pkg = clazz.packagee?.name.orEmpty()
                (pkg == "$featurePkg.impl" || pkg.startsWith("$featurePkg.impl.")) &&
                    clazz.text.contains("Destination<$keyName>")
            }
        }

        if (violations.isNotEmpty()) {
            fail(
                "every *Key at a feature's root needs a Destination<Key> implementation " +
                    "under that feature's .impl, or MainActivity's lookup fails at runtime " +
                    "instead of build time:\n" + violations.describeFiles()
            )
        }
    }

    @Test
    fun `feature impl and core internal are never reached from outside their own module`() {
        val internalLeak = Regex("""^app\.nukemichi\.android\.core\.([a-zA-Z0-9]+)\.internal(\.|$)""")
        val implLeak = Regex("""^app\.nukemichi\.android\.feature\.([a-zA-Z0-9]+)\.impl(\.|$)""")
        val violations = mutableListOf<String>()

        projectFiles.forEach { file ->
            val filePkg = file.packagee?.name ?: return@forEach
            file.imports.forEach { import ->
                val importName = import.name

                internalLeak.find(importName)?.let { match ->
                    val module = match.groupValues[1]
                    val allowed = listOf("core.$module.internal", "core.$module.di")
                        .map { "app.nukemichi.android.$it" }
                    if (allowed.none { filePkg == it || filePkg.startsWith("$it.") }) {
                        violations += "${file.describeLocation()} imports $importName"
                    }
                }

                implLeak.find(importName)?.let { match ->
                    val feature = match.groupValues[1]
                    val allowed = "app.nukemichi.android.feature.$feature.impl"
                    if (filePkg != allowed && !filePkg.startsWith("$allowed.")) {
                        violations += "${file.describeLocation()} imports $importName"
                    }
                }
            }
        }

        if (violations.isNotEmpty()) {
            fail(
                "reached into a module's .internal/.impl from outside it - depend on the " +
                    "public interface/*Key instead (core.<module>.di may see .internal, " +
                    "it's the one place that has to name the impl to bind it):\n" +
                    violations.joinToString("\n") { " - $it" }
            )
        }
    }

    @Test
    fun `feature domain layers do not depend on UI structures`() {
        val domainPackage = Regex("""^app\.nukemichi\.android\.feature\.[a-zA-Z0-9]+\.impl\.domain(\.|$)""")
        val bannedImportPrefixes = listOf("androidx.compose.", "app.nukemichi.android.core.ui.")
        val violations = mutableListOf<String>()

        projectFiles.filter { domainPackage.containsMatchIn(it.packagee?.name.orEmpty()) }
            .forEach { file ->
                file.imports.forEach { import ->
                    if (bannedImportPrefixes.any { import.name.startsWith(it) }) {
                        violations += "${file.describeLocation()} imports ${import.name}"
                    }
                }
            }

        if (violations.isNotEmpty()) {
            fail(
                "domain must not depend on UI structures (Compose, core.ui) - that dependency " +
                    "belongs in the ui layer:\n" + violations.joinToString("\n") { " - $it" }
            )
        }
    }

    @Test
    fun `feature ui layers do not reach past domain straight into core internal`() {
        val uiPackage = Regex("""^app\.nukemichi\.android\.feature\.[a-zA-Z0-9]+\.impl\.ui(\.|$)""")
        val coreInternal = Regex("""^app\.nukemichi\.android\.core\.[a-zA-Z0-9]+\.internal(\.|$)""")
        val violations = mutableListOf<String>()

        projectFiles.filter { uiPackage.containsMatchIn(it.packagee?.name.orEmpty()) }
            .forEach { file ->
                file.imports.forEach { import ->
                    if (coreInternal.containsMatchIn(import.name)) {
                        violations += "${file.describeLocation()} imports ${import.name}"
                    }
                }
            }

        if (violations.isNotEmpty()) {
            fail(
                "ui must only reach core through domain, never straight into " +
                    "core.<module>.internal:\n" + violations.joinToString("\n") { " - $it" }
            )
        }
    }

    @Test
    fun `core packages never depend on feature packages`() {
        val violations = mutableListOf<String>()

        projectFiles.filter { it.packagee?.name.orEmpty().let { pkg -> pkg == "app.nukemichi.android.core" || pkg.startsWith("app.nukemichi.android.core.") } }
            .forEach { file ->
                file.imports.forEach { import ->
                    if (import.name.startsWith("app.nukemichi.android.feature.")) {
                        violations += "${file.describeLocation()} imports ${import.name}"
                    }
                }
            }

        if (violations.isNotEmpty()) {
            fail(
                "core must never depend on feature - the dependency direction is feature -> " +
                    "core, never the other way:\n" + violations.joinToString("\n") { " - $it" }
            )
        }
    }

    private fun rootFeatureFiles(): List<KoFileDeclaration> = projectFiles.filter { file ->
        val pkg = file.packagee?.name ?: return@filter false
        val match = featurePackageRegex.find(pkg) ?: return@filter false
        match.groupValues[3].isEmpty()
    }

    private fun KoFileDeclaration.fileName(): String = File(path).name

    private fun KoFileDeclaration.describeLocation(): String = "${packagee?.name}.${fileName()}"

    private fun List<KoClassDeclaration>.describeClasses(): String =
        joinToString("\n") { " - ${it.packagee?.name}.${it.name}" }

    private fun List<KoFileDeclaration>.describeFiles(): String =
        joinToString("\n") { " - ${it.describeLocation()}" }
}
