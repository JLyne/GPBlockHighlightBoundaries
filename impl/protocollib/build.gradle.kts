plugins {
    id("gp-block-highlight-boundaries.java-conventions")
}

dependencies {
    implementation(project(":BlockHighlightBoundaries-core"))

    compileOnly(libs.spigotApi)
    compileOnly(libs.annotations)
    compileOnly(libs.griefPrevention)
    compileOnly(libs.protocolLib)
    compileOnly(libs.netty)
}
