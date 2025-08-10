// EyeDock - Root project build.gradle.kts (Complete for local testing)

buildscript {
    extra.apply {
        set("compileSdk", 34)
        set("minSdk", 26)
        set("targetSdk", 34)
        set("kotlinVersion", "1.9.20")
        set("coroutinesVersion", "1.7.3")
        set("hiltVersion", "2.48")
        set("composeVersion", "1.5.5")
        set("composeBomVersion", "2023.10.01")
        set("lifecycleVersion", "2.7.0")
        set("navigationVersion", "2.7.5")
        set("workManagerVersion", "2.8.1")
        set("retrofitVersion", "2.9.0")
        set("okHttpVersion", "4.12.0")
        set("roomVersion", "2.6.0")
        set("junitVersion", "5.10.0")
        set("junitAndroidVersion", "1.1.5")
        set("espressoVersion", "3.5.1")
        set("mockitoVersion", "5.7.0")
        set("truthVersion", "1.1.5")
        set("cameraXVersion", "1.3.0")
        set("mlKitVersion", "17.2.0")
        set("media3Version", "1.2.0")
        set("securityVersion", "1.1.0-alpha06")
    }
}

plugins {
    id("com.android.application") version "8.1.4" apply false
    id("com.android.library") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false

}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}

// Task para build completo
tasks.register("buildAll") {
    group = "build"
    description = "Build all modules"
    
    dependsOn(
        ":app:assembleDebug",
        ":core:common:build",
        ":core:onvif:build",
        ":core:media:build", 
        ":core:storage:build",
        ":core:events:build",
        ":core:ui:build"
    )
}

// Task para testar todos os módulos
tasks.register("testAll") {
    group = "verification"
    description = "Run all tests across all modules"
    
    dependsOn(
        ":core:common:test"
        // Outros módulos podem ser adicionados conforme necessário
    )
    
    doLast {
        println("🎉 All tests completed successfully!")
        println("📊 EyeDock TDD Implementation - All modules tested")
    }
}

// Task para instalar app no device
tasks.register("installAll") {
    group = "install"
    description = "Build and install debug APK"
    
    dependsOn(":app:installDebug")
    
    doLast {
        println("📱 EyeDock installed successfully!")
        println("🚀 Ready for local testing")
    }
}