plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "com.termlabs"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.3")
        bundledPlugin("com.intellij.java")
        instrumentationTools()
    }
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

intellijPlatform {
    pluginConfiguration {
        id = "com.conch.minecraft-admin"
        name = "Conch Minecraft Admin"
        version = project.version.toString()
        description = """
            Bottom-bar admin console for Paper servers behind Cube Coders AMP.
            Live status, player list, console tail, lifecycle control, broadcast and
            RCON commands. Requires the Conch Workbench platform (which provides the
            credential vault this plugin reads passwords from).
        """.trimIndent()
        ideaVersion {
            sinceBuild = "243"
            untilBuild = "251.*"
        }
    }
}

tasks.test {
    useJUnitPlatform()
    jvmArgs(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.desktop/javax.swing=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.swing=ALL-UNNAMED",
        "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "--add-opens=java.desktop/java.awt.peer=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.java2d=ALL-UNNAMED"
    )
}

// The com.conch.sdk.CredentialProvider interface is a compile-time stub —
// at runtime, Conch's vault plugin provides the real implementation via its
// own classloader. Don't ship the stub in the plugin jar or the two copies
// would conflict at class identity check time.
tasks.jar {
    exclude("com/conch/sdk/**")
}

tasks.buildPlugin {
    archiveFileName.set("termlabs-amp-minecraft-admin-plugin-${project.version}.zip")
}

// The IntelliJ plugin verifier requires downloading multiple target IDE
// versions and runs expensive checks. Skip it for local development;
// enable it manually via --info if you want to verify compatibility.
tasks.named("verifyPlugin").configure { enabled = false }
