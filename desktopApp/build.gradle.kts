plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "21"
            }
        }
    }
    
    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.desktop.currentOs)
                implementation(libs.koin.core)
                implementation(libs.sqldelight.sqlite)
                implementation(libs.coroutines.swing)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.maheswara660.flyingbird.desktop.MainKt"
        nativeDistributions {
            // Support EXE, MSI (Windows), DMG, PKG (macOS), DEB, RPM (Linux)
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Pkg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Rpm
            )
            packageName = "FlyingBird"
            packageVersion = "1.0.0"
            description = "Survive the fallen world. A post-apocalyptic arcade game of hope and decay."
            vendor = "Maheswara660"

            windows {
                packageVersion = "1.0.0"
                menu = true
                shortcut = true
                dirChooser = true
                menuGroup = "Flying Bird"
                iconFile.set(project.file("src/desktopMain/resources/AppIcon.ico"))
            }

            macOS {
                packageVersion = "1.0.0"
                bundleID = "com.maheswara660.flyingbird"
                iconFile.set(project.file("src/desktopMain/resources/AppIcon.icns"))
            }

            linux {
                packageVersion = "1.0.0"
                shortcut = true
                menuGroup = "Game"
                iconFile.set(project.file("src/desktopMain/resources/AppIcon.png"))
            }
        }
    }
}

// Custom tasks to package JPackage output as Linux archives (.tar.gz, .tar.xz)
tasks.register<Tar>("packageLinuxTarGz") {
    dependsOn("createDistributable")
    group = "distribution"
    description = "Packages the Linux application as a .tar.gz archive"
    archiveBaseName.set("FlyingBird")
    archiveVersion.set("1.0.0")
    archiveClassifier.set("linux-x64")
    archiveExtension.set("tar.gz")
    compression = Compression.GZIP

    from(layout.buildDirectory.dir("compose/binaries/main/app"))
    into("FlyingBird")
}

tasks.register<Exec>("packageLinuxTarXz") {
    dependsOn("createDistributable")
    group = "distribution"
    description = "Packages the Linux application as a .tar.xz archive"

    val buildDir = layout.buildDirectory.get().asFile
    doFirst {
        File(buildDir, "distributions").mkdirs()
    }

    workingDir = buildDir
    commandLine("tar", "-cJf", "distributions/FlyingBird-1.0.0-linux-x64.tar.xz", "-C", "compose/binaries/main", "app")
}
