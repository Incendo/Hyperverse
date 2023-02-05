pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}

rootProject.name = "Hyperverse"

include(":hyperverse-nms-common")
include(":hyperverse-core")

include(":hyperverse-nms-1-17")
include(":hyperverse-nms-1-18")
include(":hyperverse-nms-1-19")
include(":hyperverse-nms-unsupported")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
