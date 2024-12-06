enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
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
include(":hyperverse-nms-1-20-6")
include(":hyperverse-nms-1-21")
include(":hyperverse-nms-1-21-3")
