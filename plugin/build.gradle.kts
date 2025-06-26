import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("gp-block-highlight-boundaries.java-conventions")
    alias(libs.plugins.pluginYml)
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":GPBlockHighlightBoundaries-core"))
    implementation(project(":GPBlockHighlightBoundaries-packetevents1"))
    implementation(project(":GPBlockHighlightBoundaries-packetevents2"))
    implementation(project(":GPBlockHighlightBoundaries-protocollib"))
    implementation(libs.planarWrappers)

    compileOnly(libs.paperApi)
    compileOnly(libs.floodgate)
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
    apiVersion = libs.versions.paperApi.get().replace(Regex("\\-R\\d.\\d-SNAPSHOT"), "")
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
