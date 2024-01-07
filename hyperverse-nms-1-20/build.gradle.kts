plugins {
    alias(libs.plugins.paperweightUserdev)
}

dependencies {
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
    compileOnly(projects.hyperverseNmsCommon)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

tasks {
    reobfJar {
        outputJar.set(file("build/libs/${project.name}-${project.version}.jar"))
    }
    assemble {
        dependsOn(reobfJar)
    }
}
