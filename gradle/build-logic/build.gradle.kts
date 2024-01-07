plugins {
    `kotlin-dsl`
    alias(libs.plugins.cloud.buildLogic.spotless)
}

repositories {
    gradlePluginPortal()
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(libs.cloud.build.logic)
    implementation(libs.gradleKotlinJvm)

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

cloudSpotless {
    licenseHeaderFile.convention(null as RegularFile?)
    ktlintVersion = libs.versions.ktlint
}
