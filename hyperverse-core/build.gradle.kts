import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin

plugins {
    id("com.github.johnrengelman.shadow")
}

apply {
    plugin<ShadowPlugin>()
}

dependencies {
    api(projects.hyperverseNmsCommon)

    compileOnlyApi("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")

    compileOnly("com.onarandombox.multiversecore:Multiverse-Core:4.2.2")
    compileOnly("com.bergerkiller.bukkit:MyWorlds:1.15.2-v1")
    compileOnly("net.ess3:EssentialsX:2.18.2")
    compileOnly("me.clip:placeholderapi:2.10.9")

    implementation("co.aikar:acf-paper:0.5.0-SNAPSHOT")
    implementation("co.aikar:taskchain-bukkit:3.7.2")
    implementation("io.papermc:paperlib:1.0.6")
    implementation("com.google.inject:guice:5.0.1") {
        exclude("com.google.guava", "guava")
    }
    implementation("com.google.inject.extensions:guice-assistedinject:5.0.1") {
        exclude("com.google.guava", "guava")
    }
    implementation("org.bstats:bstats-bukkit:1.8")
    implementation("org.spongepowered:configurate-hocon:4.0.0")
    implementation("cloud.commandframework:cloud-paper:1.4.0")
    implementation("cloud.commandframework:cloud-minecraft-extras:1.4.0")
    implementation("cloud.commandframework:cloud-annotations:1.4.0")
    implementation("net.kyori:adventure-platform-bukkit:4.0.0-SNAPSHOT")
    implementation("net.kyori:adventure-text-minimessage:4.0.0-SNAPSHOT")

    implementation(projects.hyperverseNms1144)
    implementation(projects.hyperverseNms1152)
    implementation(projects.hyperverseNms1165)
    implementation(projects.hyperverseNmsUnsupported)
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
            exclude(project(":hyperverse-nms-1-14-4"))
            exclude(project(":hyperverse-nms-1-15-2"))
            exclude(project(":hyperverse-nms-1-16-5"))
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
        relocate("co.aikar.util", "se.hyperver.hyperverse.libs.aikar.util")
        relocate("co.aijar.taskchain", "se.hyperver.hyperverse.lubs.taskchain")
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
