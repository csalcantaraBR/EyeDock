package com.eyedock.app.utils

import android.util.Log
import com.eyedock.app.BuildConfig

/**
 * Centralized logging utility for the EyeDock application.
 * Provides consistent logging across the app with proper debug/release handling.
 */
object Logger {
    
    private const val TAG = "EyeDock"
    
    /**
     * Log a debug message
     */
    fun d(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG_LOGGING) {
            Log.d(tag, message)
        }
    }
    
    /**
     * Log an info message
     */
    fun i(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG_LOGGING) {
            Log.i(tag, message)
        }
    }
    
    /**
     * Log a warning message
     */
    fun w(message: String, tag: String = TAG) {
        Log.w(tag, message)
    }
    
    /**
     * Log an error message
     */
    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        Log.e(tag, message, throwable)
    }
    
    /**
     * Log a verbose message
     */
    fun v(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG_LOGGING) {
            Log.v(tag, message)
        }
    }
    
    /**
     * Log with a specific component tag
     */
    fun withTag(component: String) = ComponentLogger(component)
    
    /**
     * Component-specific logger
     */
    class ComponentLogger(private val component: String) {
        private val tag = "$TAG-$component"
        
        fun d(message: String) = Logger.d(message, tag)
        fun i(message: String) = Logger.i(message, tag)
        fun w(message: String) = Logger.w(message, tag)
        fun e(message: String, throwable: Throwable? = null) = Logger.e(message, throwable, tag)
        fun v(message: String) = Logger.v(message, tag)
    }
}
