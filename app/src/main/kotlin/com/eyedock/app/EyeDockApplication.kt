package com.eyedock.app

import android.app.Application
import androidx.media3.common.util.UnstableApi
import com.eyedock.app.di.AppModule

/**
 * Main application class for EyeDock.
 * Initializes dependency injection and application-wide configurations.
 */
class EyeDockApplication : Application() {
    
    @UnstableApi
    override fun onCreate() {
        super.onCreate()
        
        // Initialize dependency injection
        AppModule.initialize(this)
        
        // Application initialized successfully
    }
}