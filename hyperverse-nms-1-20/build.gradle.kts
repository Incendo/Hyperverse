plugins {
    id("hyperverse.base-conventions")
    alias(libs.plugins.paperweightUserdev)
}

dependencies {
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
    compileOnly(projects.hyperverseNmsCommon)
}

tasks {
    reobfJar {
        outputJar.set(file("build/libs/${project.name}-${project.version}.jar"))
    }
    assemble {
        dependsOn(reobfJar)
    }
}
