plugins {
    id("io.papermc.paperweight.userdev") version "1.5.3"
}

dependencies {
    paperDevBundle("1.19.4-R0.1-SNAPSHOT")
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
