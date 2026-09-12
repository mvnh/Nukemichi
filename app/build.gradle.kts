import java.util.Properties

// DownloadLibV2rayTask, DownloadGeositeDatTask, VerifyGeoipDatTask, RegenerateGeoipDatTask and
// their shared ChecksumUtil all live in buildSrc/src/main/kotlin/ - separate compiled files, not
// inline here.

val libv2rayVersion = "v26.8.20"

// Filename carries the version so bumping libv2rayVersion is itself a cache miss. Otherwise the
// task's own "if (target.exists()) return" would keep serving a stale local build/ artifact.
val downloadedLibv2rayAarFile = layout.buildDirectory.file("generated/libv2ray/libv2ray-$libv2rayVersion.aar")

val downloadLibV2ray = tasks.register<DownloadLibV2rayTask>("downloadLibV2ray") {
    group = "build setup"
    description = "Downloads and verifies the libv2ray.aar xray-core Android library"

    version.set(libv2rayVersion)
    // Must be updated together with the version above. Taken from the release asset's own digest.
    sha256.set("670cf11d9d10a6bb6548ac4f593acfa4339155732f6f8de4d45923f30a74deed")
    outputFile.set(downloadedLibv2rayAarFile)
}

val libv2rayAar: Provider<RegularFile> = downloadedLibv2rayAarFile

// Country-based direct routing (see XrayRoutingFactory): geosite.dat drives the domain half,
// geoip.dat the IP half. Both are read from the app's filesDir at runtime, not from the APK's
// assets directly - GeoAssetInstaller copies them there on first use of a version it hasn't
// staged yet.
val geositeVersion = "20260908094002"

// Staged under a versioned directory (like downloadedLibv2rayAarFile) so bumping the version is
// itself a cache miss, but the leaf filename stays the flat "geosite.dat" the app opens by name.
val geositeStagingDir = layout.buildDirectory.dir("generated/geosite/$geositeVersion")
val downloadedGeositeDatFile = geositeStagingDir.map { it.file("geosite.dat") }

val downloadGeositeDat = tasks.register<DownloadGeositeDatTask>("downloadGeositeDat") {
    group = "build setup"
    description = "Downloads and verifies geosite.dat (v2fly/domain-list-community) for country-based direct routing"

    version.set(geositeVersion)
    // Computed locally from the release asset, not the project's own sha256sum file (which
    // returned empty over plain HTTP) - see the geosite research in this session.
    sha256.set("35ed26a24cafa1256bd7261414224b7bcef5c944cea7760e172b030a8b266450")
    outputFile.set(downloadedGeositeDatFile)
}

// geoip.dat is vendored (app/src/main/assets/geoip.dat), not downloaded - see
// tools/geoip-dat/README.md for why. This only verifies the committed file matches what's pinned.
val geoipDatSha256 = "c8cce77b4d57088431b4eb543b4e06c5581204a7eec4f66b5812f3295c251216"

val verifyGeoipDat = tasks.register<VerifyGeoipDatTask>("verifyGeoipDat") {
    group = "verification"
    description = "Verifies app/src/main/assets/geoip.dat against the pinned SHA-256"

    geoipDat.set(layout.projectDirectory.file("src/main/assets/geoip.dat"))
    expectedSha256.set(geoipDatSha256)
}

tasks.register<RegenerateGeoipDatTask>("regenerateGeoipDat") {
    group = "build setup"
    description = "Maintainer-only: rebuilds app/src/main/assets/geoip.dat from a fresh DB-IP snapshot. Needs Go. See tools/geoip-dat/README.md"

    // Pinned so the generator's own behaviour is reproducible even though its input data isn't.
    geoipGeneratorCommit.set("fd96fbac6cffc06ab9a10d6ee8fad61afe9b771c")
    generatorConfig.set(rootProject.file("tools/geoip-dat/config.json"))
    outputFile.set(layout.projectDirectory.file("src/main/assets/geoip.dat"))
    outputs.upToDateWhen { false } // always talks to the network for fresh data; never "up to date"
}

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
        versionCode = gitCommitCount.getOrElse(1)
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Single source of truth for GeoAssetInstaller's cache-bust key, so bumping the pinned
        // version/checksum here automatically invalidates what's staged in the app's filesDir -
        // no separate constant to remember to update in Kotlin.
        buildConfigField("String", "GEOSITE_DAT_VERSION", "\"$geositeVersion\"")
        buildConfigField("String", "GEOIP_DAT_SHA256", "\"$geoipDatSha256\"")

        externalNativeBuild {
            ndkBuild {
                // hev-jni.c's JNI_OnLoad does FindClass(PKGNAME "/" CLSNAME) to bind its native
                // methods, so this must match TProxyService.kt's actual package, or the class
                // lookup fails at library-load time and the process aborts. Android.mk never
                // forwards an ndk-build `arguments()` variable into LOCAL_CFLAGS, so it has to be
                // injected here instead: cFlags is applied globally as -D flags by ndk-build.
                cFlags("-O3", "-DPKGNAME=app/nukemichi/android/core/vpn/internal")
            }
        }
    }

    externalNativeBuild {
        ndkBuild {
            path = file("src/main/cpp/hev-socks5-tunnel/Android.mk")
        }
    }

    sourceSets {
        getByName("main") {
            // geosite.dat is downloaded, not committed (see downloadGeositeDat below - ordering
            // relies on the same preBuild.dependsOn wiring as downloadLibV2ray, not on Gradle
            // inferring a task dependency from this srcDir); geoip.dat already lives in
            // src/main/assets and is picked up from there as usual.
            // AGP's legacy AndroidSourceSet API refuses a Provider here ("cannot determine if it
            // points to a generated or static directory") - resolving eagerly is fine since the
            // path itself is static (just "build/generated/geosite/$geositeVersion"), only the
            // file inside it is produced later, by downloadGeositeDat via the preBuild wiring below.
            assets.srcDir(geositeStagingDir.get().asFile)
        }
    }

    signingConfigs {
        val storePath = signingValue("storeFile", "NUKEMICHI_KEYSTORE_FILE")
        val store = signingValue("storePassword", "NUKEMICHI_KEYSTORE_PASSWORD")
        val alias = signingValue("keyAlias", "NUKEMICHI_KEY_ALIAS")
        val aliasPassword = signingValue("keyPassword", "NUKEMICHI_KEY_PASSWORD")

        // All four or none. Supplying some of them used to silently produce an unsigned release
        // APK, which looks exactly like the intentionally unsigned one CI builds - the difference
        // only shows up at install time, on whoever was handed the artifact.
        val supplied = listOfNotNull(storePath, store, alias, aliasPassword)
        require(supplied.isEmpty() || supplied.size == 4) {
            "Release signing is half-configured (${supplied.size}/4 values present). Set storeFile, " +
                "storePassword, keyAlias and keyPassword together, or leave all of them unset for an " +
                "unsigned build."
        }

        // Repeats what the require above already guarantees, because only the explicit null checks
        // smart-cast these to non-null for the block below.
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
            // A device that isn't arm64/x86_64 gets no split it can install. This is the
            // fallback for that case, not the recommended everyday download.
            isUniversalApk = true
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
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

    implementation(libs.sshj)
    implementation(libs.bcpkix.jdk18on)
    implementation(libs.bcprov.jdk18on)
    implementation(libs.timber)

    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Xray-core, as a prebuilt gomobile Android library
    implementation(files(libv2rayAar))

    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.collections.immutable)
}

tasks.named("preBuild") {
    dependsOn(downloadLibV2ray, downloadGeositeDat, verifyGeoipDat)
}
