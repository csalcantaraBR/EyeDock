package com.eyedock.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EyeDockApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // WorkManager sera configurado via Hilt
    }
}