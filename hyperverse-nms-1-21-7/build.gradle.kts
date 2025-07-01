plugins {
    id("hyperverse.base-conventions")
    alias(libs.plugins.paperweight.userdev)
}

indra {
    javaVersions {
        minimumToolchain(21)
        target(21)
    }
}

dependencies {
    paperweight.paperDevBundle("1.21.7-R0.1-SNAPSHOT")
    compileOnly(projects.hyperverseNmsCommon)
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}
