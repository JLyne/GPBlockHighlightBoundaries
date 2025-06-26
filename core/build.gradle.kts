plugins {
    id("gp-block-highlight-boundaries.java-conventions")
}

dependencies {
    compileOnly(libs.spigotApi)
    compileOnly(libs.annotations)
    compileOnly(libs.netty)
    compileOnly(libs.griefPrevention)
}
