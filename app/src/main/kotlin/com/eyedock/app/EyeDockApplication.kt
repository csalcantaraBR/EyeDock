package com.eyedock.app

import android.app.Application
import com.eyedock.app.di.AppModule

/**
 * Main application class for EyeDock.
 * Initializes dependency injection and application-wide configurations.
 */
class EyeDockApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize dependency injection
        AppModule.initialize(this)
        
        // Application initialized successfully
    }
}