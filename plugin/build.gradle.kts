import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("gp-block-highlight-boundaries.java-conventions")
    alias(libs.plugins.pluginYml)
}

dependencies {
    implementation(project(":GPBlockHighlightBoundaries-core"))
    implementation(project(":GPBlockHighlightBoundaries-packetevents2"))
    implementation(project(":GPBlockHighlightBoundaries-protocollib"))
    implementation(project(":GPBlockHighlightBoundaries-paperweight"))
    paperLibrary(libs.planarWrappers)
    paperLibrary(libs.messagesHelper)

    compileOnly(libs.paperApi)
    compileOnly(libs.floodgate)
    compileOnly(libs.griefPrevention)
}

tasks {
    jar {
      from(project(":GPBlockHighlightBoundaries-core").sourceSets.main.get().output)
      from(project(":GPBlockHighlightBoundaries-packetevents2").sourceSets.main.get().output)
      from(project(":GPBlockHighlightBoundaries-protocollib").sourceSets.main.get().output)
      from(project(":GPBlockHighlightBoundaries-paperweight").sourceSets.main.get().output)
    }
}

paper {
    main = "com.github.gpaddons.blockhighlightboundaries.GPBlockHighlightBoundaries"
    loader = "com.github.gpaddons.blockhighlightboundaries.GPBlockHighlightBoundariesLoader"
    apiVersion = libs.versions.paper.get().replace(Regex("\\-R\\d.\\d-SNAPSHOT"), "")
    generateLibrariesJson = true
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
