plugins {
    id("gp-block-highlight-boundaries.java-conventions")
}

dependencies {
    implementation(project(":BlockHighlightBoundaries-core"))

    compileOnly(libs.spigotApi)
    compileOnly(libs.netty)
    compileOnly(libs.annotations)
    compileOnly(libs.griefPrevention)
    compileOnly(libs.packetEvents1)
}
