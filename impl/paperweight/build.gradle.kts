plugins {
    id("gp-block-highlight-boundaries.java-conventions")
    alias(libs.plugins.paperweightUserdev)
}

dependencies {
    implementation(project(":GPBlockHighlightBoundaries-core"))
    paperweightDevelopmentBundle(libs.paperweightBundle)
    compileOnly(libs.griefPrevention)
}
