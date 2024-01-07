enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
    }
    includeBuild("gradle/build-logic")
}

rootProject.name = "Hyperverse"

include(":hyperverse-nms-common")
include(":hyperverse-core")

include(":hyperverse-nms-unsupported")
include(":hyperverse-nms-1-17")
include(":hyperverse-nms-1-18")
include(":hyperverse-nms-1-19")
include(":hyperverse-nms-1-20")
