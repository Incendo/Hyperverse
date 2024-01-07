plugins {
    id("io.papermc.paperweight.userdev") version "1.5.10"
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