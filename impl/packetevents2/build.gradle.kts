plugins {
    id("gp-block-highlight-boundaries.java-conventions")
}

dependencies {
    implementation(project(":BlockHighlightBoundaries-core"))

    compileOnly(libs.spigotApi)
    compileOnly(libs.griefPrevention)
    compileOnly(libs.packetEvents2)
}
