import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin

plugins {
    id("com.github.johnrengelman.shadow")
}

apply {
    plugin<ShadowPlugin>()
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

dependencies {
    api(projects.hyperverseNmsCommon)

    compileOnlyApi(libs.paper)

    compileOnly("com.onarandombox.multiversecore:Multiverse-Core:4.3.1")
    compileOnly("com.bergerkiller.bukkit:MyWorlds:1.20.4-v3")
    compileOnly("net.essentialsx:EssentialsX:2.20.1")
    compileOnly("me.clip:placeholderapi:2.11.5")

    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation(libs.taskchain)
    implementation(libs.paperlib)
    implementation(libs.guice) {
        exclude("com.google.guava", "guava")
    }
    implementation(libs.assistedInject) {
        exclude("com.google.guava", "guava")
    }
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("org.spongepowered:configurate-hocon:4.1.2")
    implementation(libs.cloudPaper)
    implementation(libs.cloudMinecraftExtras)
    implementation(libs.cloudMinecraftExtras)
    implementation("net.kyori:adventure-platform-bukkit:4.3.2")
    implementation("net.kyori:adventure-text-minimessage:4.14.0")

    implementation(projects.hyperverseNmsUnsupported)
    runtimeOnly(project(":hyperverse-nms-1-17", "reobf"))
    runtimeOnly(project(":hyperverse-nms-1-18", "reobf"))
    runtimeOnly(project(":hyperverse-nms-1-19", "reobf"))
    runtimeOnly(project(":hyperverse-nms-1-20", "reobf"))
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        minimize {
            exclude(project(":hyperverse-nms-unsupported"))
            exclude(project(":hyperverse-nms-1-17"))
            exclude(project(":hyperverse-nms-1-18"))
            exclude(project(":hyperverse-nms-1-19"))
            exclude(project(":hyperverse-nms-1-20"))
        }
        mergeServiceFiles()

        dependencies {
            exclude {
                it.moduleGroup == "com.google.guava"
            }
        }

        relocate("io.papermc.lib", "se.hyperver.hyperverse.libs.paperlib")
        relocate("org.bstats", "se.hyperver.hyperverse.libs.bstats")
        relocate("co.aikar.commands", "se.hyperver.hyperverse.libs.aikar.commands")
        relocate("co.aikar.locales", "se.hyperver.hyperverse.libs.aikar.locales")
        relocate("co.aikar.taskchain", "se.hyperver.hyperverse.libs.taskchain")
        relocate("co.aikar.util", "se.hyperver.hyperverse.libs.aikar.util")
        relocate("net.jodah.expiringmap", "se.hyperver.hyperverse.libs.expiringmap")
        relocate("net.kyori", "se.hyperver.hyperverse.libs.kyori")
        relocate("cloud.commandframework", "se.hyperver.hyperverse.libs.cloud")
        relocate("org.spongepowered.configurate", "se.hyperver.hyperverse.libs.configurate")
        relocate("io.leangen.geantyref", "se.hyperver.hyperverse.libs.geantyref")
        relocate("org.checkerframework", "se.hyperver.hyperverse.libs.checkerframework")
        relocate("com.typesafe.config", "se.hyperver.hyperverse.libs.hocon")
        relocate("com.google.inject", "se.hyperver.hyperverse.libs.guice")
        relocate("javax.inject", "se.hyperver.hyperverse.libs.javax.inject")
        relocate("org.aopalliance", "se.hyperver.hyperverse.libs.aop")
        relocate("javax.annotation", "se.hyperver.hyperverse.libs.javax.annotation")
    }

    build {
        dependsOn(shadowJar)
    }
}
