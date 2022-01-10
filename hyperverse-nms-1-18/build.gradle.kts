plugins {
    id("io.papermc.paperweight.userdev") version "1.3.3"
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")
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
