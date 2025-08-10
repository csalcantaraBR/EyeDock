plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
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
            
            // Enable logging for debugging
            buildConfigField("boolean", "DEBUG_LOGGING", "true")
            buildConfigField("String", "LOG_TAG", "\"EyeDock\"")
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:onvif"))
    implementation(project(":core:media"))
    implementation(project(":core:storage"))
    implementation(project(":core:events"))
    implementation(project(":core:ui"))

    // Android Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${rootProject.extra["lifecycleVersion"]}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${rootProject.extra["lifecycleVersion"]}")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:${rootProject.extra["composeBomVersion"]}")
    implementation(composeBom)
    
    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:${rootProject.extra["navigationVersion"]}")
    
    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:${rootProject.extra["hiltVersion"]}")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    kapt("com.google.dagger:hilt-compiler:${rootProject.extra["hiltVersion"]}")
    kapt("androidx.hilt:hilt-compiler:1.1.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:${rootProject.extra["workManagerVersion"]}")

    // Room Database
    implementation("androidx.room:room-runtime:${rootProject.extra["roomVersion"]}")
    implementation("androidx.room:room-ktx:${rootProject.extra["roomVersion"]}")
    ksp("androidx.room:room-compiler:${rootProject.extra["roomVersion"]}")

    // Network
    implementation("com.squareup.retrofit2:retrofit:${rootProject.extra["retrofitVersion"]}")
    implementation("com.squareup.retrofit2:converter-gson:${rootProject.extra["retrofitVersion"]}")
    implementation("com.squareup.okhttp3:okhttp:${rootProject.extra["okHttpVersion"]}")
    implementation("com.squareup.okhttp3:logging-interceptor:${rootProject.extra["okHttpVersion"]}")

    // Camera & ML Kit
    implementation("androidx.camera:camera-camera2:${rootProject.extra["cameraXVersion"]}")
    implementation("androidx.camera:camera-lifecycle:${rootProject.extra["cameraXVersion"]}")
    implementation("androidx.camera:camera-view:${rootProject.extra["cameraXVersion"]}")
    implementation("com.google.mlkit:barcode-scanning:${rootProject.extra["mlKitVersion"]}")

    // Media (ExoPlayer)
    implementation("androidx.media3:media3-exoplayer:${rootProject.extra["media3Version"]}")
    implementation("androidx.media3:media3-exoplayer-rtsp:${rootProject.extra["media3Version"]}")
    implementation("androidx.media3:media3-ui:${rootProject.extra["media3Version"]}")
    implementation("androidx.media3:media3-common:${rootProject.extra["media3Version"]}")

    // Security
    implementation("androidx.security:security-crypto:${rootProject.extra["securityVersion"]}")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${rootProject.extra["coroutinesVersion"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${rootProject.extra["coroutinesVersion"]}")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Image Loading (for thumbnails)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Testing - Unit Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${rootProject.extra["junitVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${rootProject.extra["junitVersion"]}")
    testImplementation("org.mockito:mockito-core:${rootProject.extra["mockitoVersion"]}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${rootProject.extra["coroutinesVersion"]}")
    testImplementation("com.google.truth:truth:${rootProject.extra["truthVersion"]}")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Testing - Instrumentation Tests
    androidTestImplementation("androidx.test.ext:junit:${rootProject.extra["junitAndroidVersion"]}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${rootProject.extra["espressoVersion"]}")
    androidTestImplementation("androidx.test.espresso:espresso-intents:${rootProject.extra["espressoVersion"]}")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    
    // Testing - Compose
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    // Testing - Hilt
    androidTestImplementation("com.google.dagger:hilt-android-testing:${rootProject.extra["hiltVersion"]}")
    kaptAndroidTest("com.google.dagger:hilt-compiler:${rootProject.extra["hiltVersion"]}")

    // Debug Tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}