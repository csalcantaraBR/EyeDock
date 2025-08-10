pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "EyeDock"

// Main application
include(":app")

// Core modules
include(":core:common")
include(":core:onvif")
include(":core:media")
include(":core:storage")
include(":core:events")
include(":core:ui")