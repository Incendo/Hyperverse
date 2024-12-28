plugins {
    id("hyperverse.base-conventions")
}

dependencies {
    compileOnly(libs.paper)
    compileOnlyApi(libs.guice)
    compileOnlyApi(libs.taskchain)
}
