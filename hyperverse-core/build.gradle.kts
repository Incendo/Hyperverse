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
    implementation(libs.paperlib)
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
    implementation(libs.cloudMinecraftExtras)

    // TODO: Remove and use native versions.
    implementation("net.kyori:adventure-platform-bukkit:4.3.2")
    implementation("net.kyori:adventure-text-minimessage:4.16.0")

    implementation(projects.hyperverseNmsUnsupported)
    runtimeOnly(project(":hyperverse-nms-1-17", "reobf"))
    runtimeOnly(project(":hyperverse-nms-1-18", "reobf"))
    runtimeOnly(project(":hyperverse-nms-1-19", "reobf"))
    runtimeOnly(project(":hyperverse-nms-1-20", "reobf"))
}

bukkit {
    name = "Hyperverse"
    website = "https://github.com/incendo/Hyperverse"
    authors = listOf("Citymonstret", "andrewandy")
    main = "org.incendo.hyperverse.Hyperverse"
    softDepend = listOf("Essentials", "Multiverse", "MyWorlds")
    apiVersion = "1.14"
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

    runServer {
        minecraftVersion("1.20.4")
    }
}
