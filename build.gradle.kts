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
    val indraVersion = "2.0.3"
    id("net.kyori.indra") version indraVersion apply false
    id("net.kyori.indra.checkstyle") version indraVersion apply false
    id("net.kyori.indra.publishing.sonatype") version indraVersion
    id("com.github.hierynomus.license") version "0.16.1" apply false
    id("com.github.johnrengelman.shadow") version "7.0.0" apply false
    id("net.ltgt.errorprone") version "2.0.1" apply false
    id("com.github.ben-manes.versions") version "0.36.0"
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
            testWith(8, 11, 16)
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
            val compiler = serviceOf<JavaToolchainService>().compilerFor {
                JavaLanguageVersion.of(8)
            }
            if (compiler.isPresent && compiler.get().metadata.languageVersion.asInt() > 9) {
                /*
                 * Attempt to use the release flag if compiler is Java 10 or newer.
                 * A bug in Java 9 prevents the release flag from working properly and was patched in Java 10
                 */
                options.release.set(8)
            } else {
                /* fall back to using the legacy compiler flags */
                sourceCompatibility = "1.8"
                targetCompatibility = "1.8"
            }

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
        maven("https://mvn.intellectualsites.com/content/groups/public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.codemc.org/repository/maven-public")
        maven("https://repo.spongepowered.org/maven")
        maven("https://repo.onarandombox.com/content/repositories/multiverse/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://ci.ender.zone/plugin/repository/everything/")
        maven("https://ci.athion.net/plugin/repository/tools/")
    }

    dependencies {
        COMPILE_ONLY_API_CONFIGURATION_NAME("org.checkerframework", "checker-qual", "3.9.1")
        TEST_IMPLEMENTATION_CONFIGURATION_NAME("org.junit.jupiter", "junit-jupiter-engine", "5.7.0")
        "errorprone"("com.google.errorprone", "error_prone_core", "2.5.1")
        COMPILE_ONLY_API_CONFIGURATION_NAME("com.google.errorprone", "error_prone_annotations", "2.5.1")
        COMPILE_ONLY_API_CONFIGURATION_NAME("org.jetbrains", "annotations", "20.1.0")
    }
}
