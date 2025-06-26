plugins {
    id("gp-block-highlight-boundaries.java-conventions")
}

dependencies {
    implementation(project(":GPBlockHighlightBoundaries-core"))

    compileOnly(libs.paperApi)
    compileOnly(libs.griefPrevention)
    compileOnly(libs.packetEvents2)
}
