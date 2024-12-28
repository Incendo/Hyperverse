import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("hyperverse.base-conventions")
    id("hyperverse.publishing-conventions")
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.pluginyml)
}

apply {
    plugin<ShadowPlugin>()
}

dependencies {
    api(projects.hyperverseNmsCommon)

    compileOnlyApi(libs.paper)

    compileOnly(libs.multiverse)
    compileOnly(libs.myworlds)
    compileOnly(libs.essentialsx)
    compileOnly(libs.placeholderapi)

    // TODO: Remove, because yuck.
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation(libs.taskchain)
    implementation(libs.guice) {
        exclude("com.google.guava", "guava")
    }
    implementation(libs.assistedInject) {
        exclude("com.google.guava", "guava")
    }
    implementation(libs.bstats)
    implementation(libs.configurateHocon)
    implementation(libs.cloudPaper)
    implementation(libs.cloudMinecraftExtras)

    implementation(projects.hyperverseNmsUnsupported)
    runtimeOnly(project(":hyperverse-nms-1-18")) {
        targetConfiguration = "reobf"
    }
    runtimeOnly(project(":hyperverse-nms-1-19")) {
        targetConfiguration = "reobf"
    }
    runtimeOnly(project(":hyperverse-nms-1-20")) {
        targetConfiguration = "reobf"
    }
    runtimeOnly(project(":hyperverse-nms-1-20-6")) {
        targetConfiguration = "reobf"
    }
    runtimeOnly(project(":hyperverse-nms-1-21")) {
        targetConfiguration = "reobf"
    }
    runtimeOnly(project(":hyperverse-nms-1-21-1")) {
        targetConfiguration = "reobf"
    }
    runtimeOnly(project(":hyperverse-nms-1-21-3")) {
        targetConfiguration = "reobf"
    }
}

bukkit {
    name = "Hyperverse"
    website = "https://github.com/incendo/Hyperverse"
    authors = listOf("Citymonstret", "andrewandy")
    main = "org.incendo.hyperverse.Hyperverse"
    softDepend = listOf("Essentials", "Multiverse", "MyWorlds")
    apiVersion = "1.18"
    permissions {
        mapOf(
            "worlds" to "Allows players to use the Hyperverse command",
            "reload" to "Allows players to reload the configuration and messages used by Hyperverse",
            "create" to "Allows players to create new worlds",
            "list" to "Allows players to list worlds",
            "teleport" to "Allows players to teleport between worlds",
            "teleport.other" to "Allows players to teleport other players between worlds",
            "teleportgroup" to "Allows players to teleport to their last location in a given profile group",
            "info" to "Allows players to view world info",
            "unload" to "Allows players to unload worlds",
            "load" to "Allows players to load worlds",
            "import" to "Allows players to import worlds",
            "find" to "Allows players to find the current world of another player",
            "flag.list" to "Allows player to list available flags",
            "flag.set" to "Allows players to set world flags",
            "flag.info" to "Allows players to view information regarding a flag in a given world",
            "gamerule.set" to "Allows players to world set gamerules",
            "delete" to "Allows players to delete worlds",
            "debugpaste" to "Allows players to create debug pastes",
            "who" to "Allows players to list players",
            "plugin.import" to "Allows players to import configurations from external plugins",
            "regenerate" to "Allows players to regenerate worlds"
        ).forEach { (permission, description) ->
            register("hyperverse.$permission") {
                this.description = description
                this.default = BukkitPluginDescription.Permission.Default.OP
            }
        }
        register("hyperverse.override.gamemode") {
            description = "Allows players to override world gamemode"
            default = BukkitPluginDescription.Permission.Default.FALSE
        }
    }
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
            exclude(project(":hyperverse-nms-1-18"))
            exclude(project(":hyperverse-nms-1-19"))
            exclude(project(":hyperverse-nms-1-20"))
            exclude(project(":hyperverse-nms-1-20-6"))
            exclude(project(":hyperverse-nms-1-21"))
            exclude(project(":hyperverse-nms-1-21-1"))
            exclude(project(":hyperverse-nms-1-21-3"))
        }
        mergeServiceFiles()

        dependencies {
            exclude {
                it.moduleGroup == "com.google.guava"
            }
        }

        relocate("org.bstats", "org.incendo.hyperverse.libs.bstats")
        relocate("co.aikar.commands", "org.incendo.hyperverse.libs.aikar.commands")
        relocate("co.aikar.locales", "org.incendo.hyperverse.libs.aikar.locales")
        relocate("co.aikar.taskchain", "org.incendo.hyperverse.libs.taskchain")
        relocate("co.aikar.util", "org.incendo.hyperverse.libs.aikar.util")
        relocate("net.jodah.expiringmap", "org.incendo.hyperverse.libs.expiringmap")
        relocate("cloud.commandframework", "org.incendo.hyperverse.libs.cloud")
        relocate("org.spongepowered.configurate", "org.incendo.hyperverse.libs.configurate")
        relocate("io.leangen.geantyref", "org.incendo.hyperverse.libs.geantyref")
        relocate("org.checkerframework", "org.incendo.hyperverse.libs.checkerframework")
        relocate("com.typesafe.config", "org.incendo.hyperverse.libs.hocon")
        relocate("com.google.inject", "org.incendo.hyperverse.libs.guice")
        relocate("javax.inject", "org.incendo.hyperverse.libs.javax.inject")
        relocate("org.aopalliance", "org.incendo.hyperverse.libs.aop")
        relocate("javax.annotation", "org.incendo.hyperverse.libs.javax.annotation")
        relocate("org.intellij.lang.annotations", "org.incendo.hyperverse.libs.intellij.annotations")
        relocate("org.jetbrains.annotations", "org.incendo.hyperverse.libs.jetbrains.annotations")
        relocate("com.google.errorprone", "org.incendo.hyperverse.libs.errorprone")
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        minecraftVersion("1.21.3")
    }
}
