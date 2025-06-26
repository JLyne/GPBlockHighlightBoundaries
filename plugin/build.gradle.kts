import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("gp-block-highlight-boundaries.java-conventions")
    alias(libs.plugins.pluginYml)
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":BlockHighlightBoundaries-core"))
    implementation(project(":BlockHighlightBoundaries-packetevents1"))
    implementation(project(":BlockHighlightBoundaries-packetevents2"))
    implementation(project(":BlockHighlightBoundaries-protocollib"))
    implementation(libs.planarWrappers)

    compileOnly(libs.spigotApi)
    compileOnly(libs.floodgate)
    compileOnly(libs.annotations)
    compileOnly(libs.griefPrevention)
}

tasks {
    shadowJar {
        archiveClassifier = ""
        relocate("com.github.jikoo.planarwrappers", "com.github.gpaddons.blockhighlightboundaries.planarwrappers")
        minimize()
    }

    build {
        dependsOn(shadowJar)
    }
}

bukkit {
    main = "com.github.gpaddons.blockhighlightboundaries.GPBlockHighlightBoundaries"
    apiVersion = "1.17"
    authors = listOf("Jim (AnEnragedPigeon)", "Jikoo")
    depend = listOf("GriefPrevention")
    softDepend = listOf("ProtocolLib", "PacketEvents", "Floodgate")

    commands {
        register("gpbhbreload") {
            description = "Reload the GPBlockHighlightBoundaries configuration."
            permission = "gpbhb.reload"
        }
    }

    permissions {
        register("gpbhb.reload") {
          description = "Allow use of /gpbhbreload"
        }
    }
}
