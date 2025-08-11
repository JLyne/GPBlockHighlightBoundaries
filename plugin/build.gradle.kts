import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

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
    implementation(project(":GPBlockHighlightBoundaries-paperweight"))
    implementation(libs.planarWrappers)
    implementation(libs.messagesHelper)

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

paper {
    main = "com.github.gpaddons.blockhighlightboundaries.GPBlockHighlightBoundaries"
    apiVersion = libs.versions.paper.get().replace(Regex("\\-R\\d.\\d-SNAPSHOT"), "")
    authors = listOf("Jim (AnEnragedPigeon)", "Jikoo")

    serverDependencies {
      register("GriefPrevention") {
        required = true
        load = PaperPluginDescription.RelativeLoadOrder.BEFORE
      }
      register("ProtocolLib") {
        required = false
        load = PaperPluginDescription.RelativeLoadOrder.BEFORE
      }
      register("PacketEvents") {
        required = false
        load = PaperPluginDescription.RelativeLoadOrder.BEFORE
      }
      register("Floodgate") {
        required = false
        load = PaperPluginDescription.RelativeLoadOrder.BEFORE
      }
    }

    permissions {
        register("gpbhb.toggle") {
          description = "Allow use of /basicvisualizations and /enhancedvisualizations"
          default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("gpbhb.reload") {
          description = "Allow use of /gpbhbreload"
          default = BukkitPluginDescription.Permission.Default.OP
        }
    }
}
