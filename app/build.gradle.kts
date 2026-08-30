import java.net.URL
import java.security.MessageDigest
import java.util.Properties

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
        if (target.exists()) return

        val url = "https://github.com/2dust/AndroidLibXrayLite/releases/download/${version.get()}/libv2ray.aar"
        val tempFile = File(temporaryDir, "libv2ray.aar")
        logger.lifecycle("⬇️ Downloading libv2ray.aar ${version.get()}...")

        try {
            URL(url).openStream().use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            verifyChecksum(tempFile)
            target.parentFile.mkdirs()
            tempFile.copyTo(target, overwrite = true)
            logger.lifecycle("✅ libv2ray.aar verified and staged at: ${target.path}")
        } catch (e: Exception) {
            logger.error("❌ Failed to download libv2ray.aar: ${e.message}")
            throw e
        }
    }

    private fun verifyChecksum(archive: File) {
        val expected = sha256.get()
        val digest = MessageDigest.getInstance("SHA-256")
        archive.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }

        if (actual != expected) {
            archive.delete()
            throw GradleException("libv2ray.aar checksum mismatch: expected $expected but got $actual.")
        }
        logger.lifecycle("🔒 Verified libv2ray.aar SHA-256")
    }
}

val libv2rayVersion = "v26.8.20"

// Filename carries the version so bumping libv2rayVersion is itself a cache miss — otherwise the
// task's own "if (target.exists()) return" would keep serving a stale local build/ artifact.
val downloadedLibv2rayAarFile = layout.buildDirectory.file("generated/libv2ray/libv2ray-$libv2rayVersion.aar")

val downloadLibV2ray = tasks.register<DownloadLibV2rayTask>("downloadLibV2ray") {
    group = "build setup"
    description = "Downloads and verifies the libv2ray.aar xray-core Android library"

    version.set(libv2rayVersion)
    // Must be updated together with version above — taken from the release asset's own digest.
    sha256.set("670cf11d9d10a6bb6548ac4f593acfa4339155732f6f8de4d45923f30a74deed")
    outputFile.set(downloadedLibv2rayAarFile)
}

val libv2rayAar: Any = downloadedLibv2rayAarFile

val keystoreProperties = Properties().apply {
    rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}

fun signingValue(property: String, environmentVariable: String): String? =
    (keystoreProperties.getProperty(property) ?: System.getenv(environmentVariable))
        ?.takeIf { it.isNotBlank() }

val gitCommitCount: Provider<Int> = if (rootProject.file(".git").exists()) {
    providers.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
        isIgnoreExitValue = true
    }.standardOutput.asText.map { it.trim().toIntOrNull() ?: 1 }
} else {
    provider { 1 }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "app.nukemichi.android"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "app.nukemichi.android"
        minSdk = 26
        targetSdk = 36
        versionCode = gitCommitCount.get()
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            ndkBuild {
                // hev-jni.c's JNI_OnLoad does FindClass(PKGNAME "/" CLSNAME) to bind its native
                // methods — this must match TProxyService.kt's actual package, or the class
                // lookup fails at library-load time and the process aborts. Android.mk never
                // forwards an ndk-build `arguments()` variable into LOCAL_CFLAGS, so it has to be
                // injected here instead — cFlags is applied globally as -D flags by ndk-build.
                cFlags("-O3", "-DPKGNAME=app/nukemichi/android/core/vpn/internal")
            }
        }
    }

    externalNativeBuild {
        ndkBuild {
            path = file("src/main/cpp/hev-socks5-tunnel/Android.mk")
        }
    }

    signingConfigs {
        val storePath = signingValue("storeFile", "NUKEMICHI_KEYSTORE_FILE")
        val store = signingValue("storePassword", "NUKEMICHI_KEYSTORE_PASSWORD")
        val alias = signingValue("keyAlias", "NUKEMICHI_KEY_ALIAS")
        val aliasPassword = signingValue("keyPassword", "NUKEMICHI_KEY_PASSWORD")

        if (storePath != null && store != null && alias != null && aliasPassword != null) {
            create("release") {
                storeFile = rootProject.file(storePath)
                storePassword = store
                keyAlias = alias
                keyPassword = aliasPassword
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "x86_64")
            // A device that isn't arm64/x86_64 gets no split it can install — this is the
            // fallback for that case, not the recommended everyday download.
            isUniversalApk = true
        }
    }
}

dependencies {
    // Android
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.konsist)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // SSH and crypto
    implementation(libs.sshj)
    implementation(libs.bcpkix.jdk18on)
    implementation(libs.bcprov.jdk18on)

    // Logging
    implementation(libs.timber)

    // DI and symbol processing
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Xray-core, as a prebuilt gomobile Android library
    implementation(files(libv2rayAar))

    // Navigation
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // Kotlin
    implementation(libs.kotlinx.collections.immutable)
}

tasks.named("preBuild") {
    dependsOn(downloadLibV2ray)
}
