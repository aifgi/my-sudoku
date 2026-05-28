import java.awt.BasicStroke
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

plugins {
    kotlin("jvm") version "2.3.21"
    id("org.jetbrains.compose") version "1.11.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21"
}

kotlin {
    jvmToolchain(maxOf(21, (findProperty("javaVersion") as String?)?.toInt() ?: 21))
}

dependencies {
    implementation(project(":engine"))
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}

tasks.test {
    useJUnitPlatform()
}

fun drawIcon(size: Int): BufferedImage {
    val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
    val g = image.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

    val radius = (size * 0.19).toInt()
    g.setColor(Color(0x3D, 0x5A, 0x9A))
    g.fillRoundRect(0, 0, size, size, radius, radius)

    val padding = (size * 0.11).toInt()
    val gridSize = size - 2 * padding
    val cellSize = gridSize / 9

    // Thin cell lines
    g.setColor(Color(0xFF, 0xFF, 0xFF, 90))
    g.setStroke(BasicStroke((size * 0.004f).coerceAtLeast(1f)))
    for (i in 1..8) {
        if (i % 3 != 0) {
            val pos = padding + i * cellSize
            g.drawLine(pos, padding, pos, padding + gridSize)
            g.drawLine(padding, pos, padding + gridSize, pos)
        }
    }

    // Thick 3x3 box lines
    g.setColor(Color.WHITE)
    g.setStroke(BasicStroke((size * 0.010f).coerceAtLeast(1f)))
    for (i in 0..3) {
        val pos = padding + i * 3 * cellSize
        g.drawLine(pos, padding, pos, padding + gridSize)
        g.drawLine(padding, pos, padding + gridSize, pos)
    }

    g.dispose()
    return image
}

val iconsDir = layout.projectDirectory.dir("src/main/icons")

val generateIconPng by tasks.registering {
    val pngFile = iconsDir.file("icon.png")
    outputs.file(pngFile)
    doLast {
        iconsDir.asFile.mkdirs()
        ImageIO.write(drawIcon(1024), "PNG", pngFile.asFile)
        logger.lifecycle("Generated icon PNG: ${pngFile.asFile.absolutePath}")
    }
}

val generateIconIcns by tasks.registering {
    dependsOn(generateIconPng)
    val icnsFile = iconsDir.file("icon.icns")
    outputs.file(icnsFile)
    onlyIf { org.gradle.internal.os.OperatingSystem.current().isMacOsX }
    doLast {
        val iconsetDir = iconsDir.dir("icon.iconset").asFile
        iconsetDir.deleteRecursively()
        iconsetDir.mkdirs()

        val sizes = listOf(16, 32, 64, 128, 256, 512, 1024)
        for (s in sizes) {
            ImageIO.write(drawIcon(s), "PNG", File(iconsetDir, "icon_${s}x${s}.png"))
            if (s <= 512) {
                ImageIO.write(drawIcon(s * 2), "PNG", File(iconsetDir, "icon_${s}x${s}@2x.png"))
            }
        }

        val exitCode = ProcessBuilder("iconutil", "-c", "icns", "-o", icnsFile.asFile.absolutePath, iconsetDir.absolutePath)
            .inheritIO()
            .start()
            .waitFor()
        require(exitCode == 0) { "iconutil failed with exit code $exitCode" }
        iconsetDir.deleteRecursively()
        logger.lifecycle("Generated icon ICNS: ${icnsFile.asFile.absolutePath}")
    }
}

compose.desktop {
    application {
        mainClass = "sudoku.app.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "Sudoku"
            packageVersion = (findProperty("packageVersion") as String?) ?: "1.0.0"
            description = "Sudoku puzzle game"
            windows {
                menuGroup = "Sudoku"
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
            }
            macOS {
                bundleID = "com.sudoku.app"
                iconFile.set(iconsDir.file("icon.icns"))
            }
            linux {
                packageName = "sudoku"
                iconFile.set(iconsDir.file("icon.png"))
            }
        }
    }
}

afterEvaluate {
    listOf("packageDmg", "createDistributable", "runDistributable").forEach { name ->
        tasks.findByName(name)?.dependsOn(generateIconIcns)
    }
    listOf("packageMsi", "packageDeb").forEach { name ->
        tasks.findByName(name)?.dependsOn(generateIconPng)
    }
}