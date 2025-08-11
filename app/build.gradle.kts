plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    id("jacoco")
}

android {
    namespace = "com.eyedock.app"
    compileSdk = rootProject.extra["compileSdk"] as Int

    defaultConfig {
        applicationId = "com.eyedock.app"
        minSdk = rootProject.extra["minSdk"] as Int
        targetSdk = rootProject.extra["targetSdk"] as Int
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            buildConfigField("boolean", "DEBUG_LOGGING", "true")
            buildConfigField("String", "LOG_TAG", "\"EyeDock\"")
        }
        
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            buildConfigField("boolean", "DEBUG_LOGGING", "false")
            buildConfigField("String", "LOG_TAG", "\"EyeDock\"")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["composeVersion"] as String
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // ===== CORE ANDROID =====
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${rootProject.extra["lifecycleVersion"]}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${rootProject.extra["lifecycleVersion"]}")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // ===== COMPOSE =====
    val composeBom = platform("androidx.compose:compose-bom:${rootProject.extra["composeBomVersion"]}")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // ===== NAVIGATION =====
    implementation("androidx.navigation:navigation-compose:${rootProject.extra["navigationVersion"]}")
    
    // ===== DATABASE =====
    implementation("androidx.room:room-runtime:${rootProject.extra["roomVersion"]}")
    implementation("androidx.room:room-ktx:${rootProject.extra["roomVersion"]}")
    ksp("androidx.room:room-compiler:${rootProject.extra["roomVersion"]}")

    // ===== NETWORKING =====
    implementation("com.squareup.retrofit2:retrofit:${rootProject.extra["retrofitVersion"]}")
    implementation("com.squareup.retrofit2:converter-gson:${rootProject.extra["retrofitVersion"]}")
    implementation("com.squareup.okhttp3:okhttp:${rootProject.extra["okHttpVersion"]}")
    implementation("com.squareup.okhttp3:logging-interceptor:${rootProject.extra["okHttpVersion"]}")

    // ===== MEDIA =====
    implementation("androidx.media3:media3-exoplayer:${rootProject.extra["media3Version"]}")
    implementation("androidx.media3:media3-exoplayer-rtsp:${rootProject.extra["media3Version"]}")
    implementation("androidx.media3:media3-ui:${rootProject.extra["media3Version"]}")
    implementation("androidx.media3:media3-common:${rootProject.extra["media3Version"]}")

    // ===== CAMERA & ML =====
    implementation("androidx.camera:camera-camera2:${rootProject.extra["cameraXVersion"]}")
    implementation("androidx.camera:camera-lifecycle:${rootProject.extra["cameraXVersion"]}")
    implementation("androidx.camera:camera-view:${rootProject.extra["cameraXVersion"]}")
    implementation("com.google.mlkit:barcode-scanning:${rootProject.extra["mlKitVersion"]}")

    // ===== UTILITIES =====
    implementation("androidx.security:security-crypto:${rootProject.extra["securityVersion"]}")
    implementation("androidx.work:work-runtime-ktx:${rootProject.extra["workManagerVersion"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${rootProject.extra["coroutinesVersion"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${rootProject.extra["coroutinesVersion"]}")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ===== TESTING =====
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:${rootProject.extra["mockitoVersion"]}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${rootProject.extra["coroutinesVersion"]}")
    testImplementation("com.google.truth:truth:${rootProject.extra["truthVersion"]}")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    androidTestImplementation("androidx.test.ext:junit:${rootProject.extra["junitAndroidVersion"]}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${rootProject.extra["espressoVersion"]}")
    androidTestImplementation("androidx.test.espresso:espresso-intents:${rootProject.extra["espressoVersion"]}")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // ===== DEBUG TOOLS =====
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}

// ===== JACOCO CONFIGURATION =====
jacoco {
    toolVersion = "0.8.11"
}

tasks.withType<Test> {
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    
    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )
    
    val debugTree = fileTree("${buildDir}/intermediates/javac/debug") {
        exclude(fileFilter)
    }
    val mainSrc = "${project.projectDir}/src/main/java"
    
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(buildDir) {
        include("/jacoco/testDebugUnitTest.exec")
    })
}