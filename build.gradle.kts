import com.hierynomus.gradle.license.LicenseBasePlugin
import com.hierynomus.gradle.license.tasks.LicenseCheck
import net.kyori.indra.IndraExtension
import net.kyori.indra.IndraPlugin
import net.kyori.indra.IndraPublishingPlugin
import net.kyori.indra.IndraCheckstylePlugin
import net.kyori.indra.repository.sonatypeSnapshots
import net.ltgt.gradle.errorprone.ErrorPronePlugin
import net.ltgt.gradle.errorprone.errorprone
import nl.javadude.gradle.plugins.license.LicenseExtension
import org.gradle.api.plugins.JavaPlugin.*
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    val indraVersion = "2.0.6"
    id("net.kyori.indra") version indraVersion apply false
    id("net.kyori.indra.checkstyle") version indraVersion apply false
    id("net.kyori.indra.publishing.sonatype") version indraVersion
    id("com.github.hierynomus.license") version "0.16.1" apply false
    id("com.github.johnrengelman.shadow") version "7.1.0" apply false
    id("net.ltgt.errorprone") version "2.0.2" apply false
    id("com.github.ben-manes.versions") version "0.39.0"
    idea
}

group = "se.hyperver.hyperverse"
version = "0.11.0-SNAPSHOT"
description = "Minecraft world management plugin"

plugins.apply("idea")

subprojects {
    apply<IndraPlugin>()
    apply<IndraPublishingPlugin>()
    apply<ErrorPronePlugin>()
    apply<LicenseBasePlugin>()

    if (this.name.startsWith("hyperverse-nms").not()) {
        apply<IndraCheckstylePlugin>()
    }

    extensions.configure(LicenseExtension::class) {
        header = rootProject.file("HEADER")
        mapping("java", "DOUBLESLASH_STYLE")
        mapping("kt", "DOUBLESLASH_STYLE")
        includes(listOf("**/*.java", "**/*.kt"))
    }

    extensions.configure(IndraExtension::class) {
        github("Incendo", "Hyperverse") {
            ci(true)
        }
        gpl3OnlyLicense()

        javaVersions {
            testWith(17)
        }
        checkstyle("8.39")

        configurePublications {
            pom {
                developers {
                    developer {
                        id.set("Sauilitired")
                        name.set("Alexander Söderberg")
                        url.set("https://alexander-soderberg.com")
                        email.set("alexander.soderberg@incendo.org")
                    }
                }
            }
        }
    }

    /* Disable checkstyle on tests */
    project.gradle.startParameter.excludedTaskNames.add("checkstyleTest")

    tasks {
        withType(JavaCompile::class) {
            javaCompiler.set(serviceOf<JavaToolchainService>().compilerFor {
                languageVersion.set(JavaLanguageVersion.of(17))
            })
            options.release.set(17)

            options.errorprone {
                /* These are just annoying */
                disable(
                        "JdkObsolete",
                        "FutureReturnValueIgnored",
                        "ImmutableEnumChecker",
                        "StringSplitter",
                        "EqualsGetClass",
                        "CatchAndPrintStackTrace",
                        "TypeParameterUnusedInFormals",
                        "EmptyCatch"
                )
            }
            // TODO: Re-enable
            // options.compilerArgs.addAll(listOf("-Xlint:-processing", "-Werror"))
        }

        withType(Javadoc::class) {
            javadocTool.set(serviceOf<JavaToolchainService>().javadocToolFor {
                languageVersion.set(JavaLanguageVersion.of(17))
            })
        }

        named("check") {
            dependsOn(withType(LicenseCheck::class))
        }
    }

    repositories {
        mavenCentral()
        sonatypeSnapshots()

        maven("https://oss.sonatype.org/content/repositories/releases") {
            mavenContent {
                releasesOnly()
            }
        }
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://repo.spongepowered.org/maven")
        maven("https://repo.onarandombox.com/content/repositories/multiverse/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://ci.mg-dev.eu/plugin/repository/everything/")
        maven("https://repo.essentialsx.net/releases/")

    }

    dependencies {
        COMPILE_ONLY_API_CONFIGURATION_NAME("org.checkerframework", "checker-qual", "3.9.1")
        TEST_IMPLEMENTATION_CONFIGURATION_NAME("org.junit.jupiter", "junit-jupiter-engine", "5.8.2")
        "errorprone"("com.google.errorprone", "error_prone_core", "2.10.0")
        COMPILE_ONLY_API_CONFIGURATION_NAME("com.google.errorprone", "error_prone_annotations", "2.5.1")
        COMPILE_ONLY_API_CONFIGURATION_NAME("org.jetbrains", "annotations", "23.0.0")
    }
}
