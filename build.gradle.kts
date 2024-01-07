plugins {
    alias(libs.plugins.cloud.buildLogic.rootProject.publishing)
    alias(libs.plugins.cloud.buildLogic.rootProject.spotless)
}

repositories {
    mavenCentral()
}

spotlessPredeclare {
    kotlin { ktlint(libs.versions.ktlint.get()) }
    kotlinGradle { ktlint(libs.versions.ktlint.get()) }
}

subprojects {
    if ("nms" in name) {
        tasks {
            whenTaskAdded {
                if ("checkstyle" in name) {
                    enabled = false
                }
            }
        }
    }

    afterEvaluate {
        tasks.withType<JavaCompile>().configureEach {
            options.compilerArgs.remove("-Werror")
        }
        tasks.findByName("spotlessConfigsCheck")?.enabled = false
    }
}

tasks {
    spotlessCheck {
        dependsOn(gradle.includedBuild("build-logic").task(":spotlessCheck") )
    }
    spotlessApply {
        dependsOn(gradle.includedBuild("build-logic").task(":spotlessApply"))
    }
}
